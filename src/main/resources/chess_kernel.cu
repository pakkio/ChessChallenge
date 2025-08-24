/*
 * CUDA Kernel for Parallel Chess Constraint Checking
 * Each thread evaluates one possible piece placement
 */

#include <stdio.h>

// Board constants
#define MAX_BOARD_SIZE 9
#define MAX_PIECES 8

// Piece types
#define KING 0
#define QUEEN 1
#define BISHOP 2
#define KNIGHT 3
#define ROOK 4

// Compact board representation using bit vectors
typedef struct {
    int m, n;                    // Board dimensions
    unsigned long long occupied; // Occupied slots as bit vector (64 bits max)
    char pieces[MAX_PIECES];     // Piece types to place
    char positions[MAX_PIECES];  // Current piece positions (encoded as x*n+y)
    int piece_count;             // Number of pieces
    int current_piece;           // Current piece being placed
} BoardState;

// Device function to check if two pieces attack each other
__device__ bool pieces_attack(int piece1_type, int pos1, int piece2_type, int pos2, int m, int n) {
    int x1 = pos1 / n, y1 = pos1 % n;
    int x2 = pos2 / n, y2 = pos2 % n;
    
    // Same position
    if (pos1 == pos2) return true;
    
    // Helper function to check if position is valid
    auto valid = [m, n](int x, int y) { return x >= 0 && x < m && y >= 0 && y < n; };
    
    // King attacks
    if (piece1_type == KING) {
        int dx = abs(x2 - x1);
        int dy = abs(y2 - y1);
        if (dx <= 1 && dy <= 1 && (dx + dy > 0)) return true;
    }
    
    // Queen attacks (rook + bishop)
    if (piece1_type == QUEEN) {
        // Horizontal/vertical (rook-like)
        if (x1 == x2 || y1 == y2) return true;
        // Diagonal (bishop-like)
        if (abs(x2 - x1) == abs(y2 - y1)) return true;
    }
    
    // Bishop attacks
    if (piece1_type == BISHOP) {
        if (abs(x2 - x1) == abs(y2 - y1)) return true;
    }
    
    // Knight attacks
    if (piece1_type == KNIGHT) {
        int dx = abs(x2 - x1);
        int dy = abs(y2 - y1);
        if ((dx == 2 && dy == 1) || (dx == 1 && dy == 2)) return true;
    }
    
    return false;
}

// Device function to check if a board state is valid
__device__ bool is_valid_board(BoardState* state) {
    // Check all pairs of placed pieces
    for (int i = 0; i < state->current_piece; i++) {
        for (int j = i + 1; j < state->current_piece; j++) {
            if (pieces_attack(state->pieces[i], state->positions[i], 
                            state->pieces[j], state->positions[j], 
                            state->m, state->n)) {
                return false;
            }
        }
    }
    return true;
}

// CUDA kernel for parallel board evaluation
__global__ void evaluate_boards(BoardState* input_states, bool* results, int num_states) {
    int tid = blockIdx.x * blockDim.x + threadIdx.x;
    
    if (tid < num_states) {
        results[tid] = is_valid_board(&input_states[tid]);
    }
}

// CUDA kernel for massively parallel placement generation
__global__ void generate_placements(
    BoardState* base_state,     // Input: base board state
    int* available_positions,   // Available positions to try
    int num_positions,          // Number of available positions
    BoardState* output_states,  // Output: generated board states
    bool* valid_flags,          // Output: validity flags
    int piece_to_place         // Which piece we're placing
) {
    int tid = blockIdx.x * blockDim.x + threadIdx.x;
    
    if (tid < num_positions) {
        // Copy base state
        output_states[tid] = *base_state;
        
        // Place piece at this position
        output_states[tid].positions[piece_to_place] = available_positions[tid];
        output_states[tid].current_piece = piece_to_place + 1;
        
        // Check if placement is valid
        valid_flags[tid] = is_valid_board(&output_states[tid]);
    }
}

// CUDA kernel for symmetry elimination (canonical form computation)
__global__ void compute_canonical_forms(
    BoardState* states,
    unsigned long long* canonical_hashes,
    int num_states,
    int m, int n
) {
    int tid = blockIdx.x * blockDim.x + threadIdx.x;
    
    if (tid < num_states) {
        BoardState* state = &states[tid];
        unsigned long long min_hash = ULLONG_MAX;
        
        // Try all 8 symmetries and find minimum hash
        for (int sym = 0; sym < 8; sym++) {
            unsigned long long hash = 0;
            
            for (int piece_idx = 0; piece_idx < state->current_piece; piece_idx++) {
                int pos = state->positions[piece_idx];
                int x = pos / n, y = pos % n;
                int new_x, new_y;
                
                // Apply symmetry transformation
                switch (sym) {
                    case 0: new_x = x; new_y = y; break;                    // Identity
                    case 1: new_x = n-1-y; new_y = x; break;               // 90° rotation
                    case 2: new_x = m-1-x; new_y = n-1-y; break;           // 180° rotation
                    case 3: new_x = y; new_y = m-1-x; break;               // 270° rotation
                    case 4: new_x = m-1-x; new_y = y; break;               // Horizontal reflection
                    case 5: new_x = x; new_y = n-1-y; break;               // Vertical reflection
                    case 6: new_x = n-1-y; new_y = m-1-x; break;           // Diagonal reflection
                    case 7: new_x = y; new_y = x; break;                   // Anti-diagonal reflection
                }
                
                int new_pos = new_x * n + new_y;
                
                // Contribute to hash (simple polynomial hash)
                hash = hash * 31 + (state->pieces[piece_idx] * 100 + new_pos);
            }
            
            min_hash = min(min_hash, hash);
        }
        
        canonical_hashes[tid] = min_hash;
    }
}

// Host function prototypes (to be called from JCuda)
extern "C" {
    void launch_evaluate_boards(BoardState* states, bool* results, int num_states, cudaStream_t stream);
    void launch_generate_placements(BoardState* base, int* positions, int num_pos, 
                                   BoardState* output, bool* valid, int piece, cudaStream_t stream);
    void launch_canonical_forms(BoardState* states, unsigned long long* hashes, 
                               int num_states, int m, int n, cudaStream_t stream);
}

void launch_evaluate_boards(BoardState* states, bool* results, int num_states, cudaStream_t stream) {
    int block_size = 256;
    int grid_size = (num_states + block_size - 1) / block_size;
    evaluate_boards<<<grid_size, block_size, 0, stream>>>(states, results, num_states);
}

void launch_generate_placements(BoardState* base, int* positions, int num_pos, 
                               BoardState* output, bool* valid, int piece, cudaStream_t stream) {
    int block_size = 256;
    int grid_size = (num_pos + block_size - 1) / block_size;
    generate_placements<<<grid_size, block_size, 0, stream>>>(base, positions, num_pos, output, valid, piece);
}

void launch_canonical_forms(BoardState* states, unsigned long long* hashes, 
                           int num_states, int m, int n, cudaStream_t stream) {
    int block_size = 256;
    int grid_size = (num_states + block_size - 1) / block_size;
    compute_canonical_forms<<<grid_size, block_size, 0, stream>>>(states, hashes, num_states, m, n);
}