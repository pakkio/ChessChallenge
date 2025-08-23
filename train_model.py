#!/usr/bin/env python3
"""
Chess Placement Predictor - Neural Network Training
Predicts probability that a piece placement leads to valid solutions
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
import joblib
import matplotlib.pyplot as plt
import seaborn as sns

def load_and_preprocess_data(filename):
    """Load and preprocess chess training data"""
    print(f"Loading data from {filename}...")
    df = pd.read_csv(filename)
    
    print(f"Dataset shape: {df.shape}")
    print(f"Positive examples: {df['successful'].sum()}")
    print(f"Negative examples: {(~df['successful']).sum()}")
    print(f"Balance: {df['successful'].mean():.3f}")
    
    # Encode piece types
    le_piece = LabelEncoder()
    df['pieceType_encoded'] = le_piece.fit_transform(df['pieceType'])
    
    # Feature engineering
    df['position_center_bias'] = 1.0 / (1.0 + df['centerDistance'])
    df['position_corner_bias'] = 1.0 / (1.0 + df['cornerDistance'])
    df['density'] = df['occupiedSquares'] / df['boardSize']
    df['pieces_left_ratio'] = df['remainingPieces'] / (df['remainingPieces'] + df['occupiedSquares'] + 1)
    df['attack_efficiency'] = df['attackedSquares'] / (df['boardSize'] - df['occupiedSquares'] + 1)
    
    # Select features for model
    feature_columns = [
        'boardSize', 'occupiedSquares', 'remainingPieces',
        'pieceType_encoded', 'candidateX', 'candidateY',
        'attackedSquares', 'safeSquares', 'cornerDistance', 'centerDistance',
        'nearbyPieces', 'threatenedByExisting', 'threatensExisting',
        'position_center_bias', 'position_corner_bias', 'density',
        'pieces_left_ratio', 'attack_efficiency'
    ]
    
    X = df[feature_columns]
    y = df['successful']
    
    return X, y, le_piece, feature_columns

def train_model(X, y):
    """Train Random Forest classifier"""
    print("Splitting data...")
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    
    print("Scaling features...")
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    print("Training Random Forest model...")
    model = RandomForestClassifier(
        n_estimators=100,
        max_depth=10,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1,
        class_weight='balanced'  # Handle imbalanced data
    )
    
    model.fit(X_train_scaled, y_train)
    
    # Evaluate model
    print("Evaluating model...")
    y_pred = model.predict(X_test_scaled)
    y_pred_proba = model.predict_proba(X_test_scaled)[:, 1]
    
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred))
    
    print(f"ROC AUC Score: {roc_auc_score(y_test, y_pred_proba):.4f}")
    
    # Feature importance
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)
    
    print("\nTop 10 Most Important Features:")
    print(feature_importance.head(10))
    
    return model, scaler, feature_importance, X_test_scaled, y_test, y_pred_proba

def save_model(model, scaler, le_piece, feature_columns):
    """Save trained model and preprocessing objects"""
    print("Saving model...")
    joblib.dump(model, 'chess_placement_model.pkl')
    joblib.dump(scaler, 'feature_scaler.pkl') 
    joblib.dump(le_piece, 'piece_encoder.pkl')
    joblib.dump(feature_columns, 'feature_columns.pkl')
    print("Model saved successfully!")

def create_visualizations(feature_importance, y_test, y_pred_proba):
    """Create visualizations for model analysis"""
    # Feature importance plot
    plt.figure(figsize=(10, 8))
    plt.subplot(2, 1, 1)
    sns.barplot(data=feature_importance.head(10), x='importance', y='feature')
    plt.title('Top 10 Feature Importances')
    plt.xlabel('Importance')
    
    # ROC curve would go here but requires sklearn.metrics.roc_curve
    plt.subplot(2, 1, 2)
    plt.hist(y_pred_proba[y_test == 1], alpha=0.7, label='Successful', bins=50)
    plt.hist(y_pred_proba[y_test == 0], alpha=0.7, label='Failed', bins=50)
    plt.xlabel('Predicted Probability')
    plt.ylabel('Count')
    plt.title('Prediction Distribution')
    plt.legend()
    
    plt.tight_layout()
    plt.savefig('model_analysis.png')
    print("Visualization saved as model_analysis.png")

def main():
    """Main training pipeline"""
    # Load data
    X, y, le_piece, feature_columns = load_and_preprocess_data('chess_training_data.csv')
    
    # Train model
    model, scaler, feature_importance, X_test_scaled, y_test, y_pred_proba = train_model(X, y)
    
    # Save model
    save_model(model, scaler, le_piece, feature_columns)
    
    # Create visualizations
    try:
        create_visualizations(feature_importance, y_test, y_pred_proba)
    except Exception as e:
        print(f"Visualization creation failed: {e}")
    
    # Test prediction function
    print("\nTesting prediction function...")
    sample_prediction = model.predict_proba(X_test_scaled[:1])
    print(f"Sample prediction probability: {sample_prediction[0][1]:.4f}")
    
    print("\nTraining complete! Model ready for integration.")

if __name__ == "__main__":
    main()