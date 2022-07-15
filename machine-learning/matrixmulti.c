#include <stdio.h>
#include <stdlib.h>

void print_matrix(int *arr, int nrow, int ncol)
{
    for (int i=0; i<nrow; i++)
    {
        for (int j=0; j<ncol; j++)
        {
            printf("%4d ", *(arr+(ncol*i+j))); 
        }
        printf("\n");
    }
    printf("\n");
}

void print_matrix_product(int *A, int *B, int *C, int mA, int nA, int mB, int nB)
{
    int mxRows = (mA > mB) ? mA : mB;
    int middle_of_matrix = (int) (mxRows / 2);

    // print the matrices (nicely) next to each other
    for (int i=0; i<mxRows; i++)
    {
        for (int j=0; j<2*nB+nA; j++)
        {
            if (i == middle_of_matrix && j == nA) printf("* ");
            else if (i != middle_of_matrix && j == nA) printf("  ");

            if (i == middle_of_matrix && j == (nA+nB)) printf("= ");
            else if (i != middle_of_matrix && j == (nA+nB)) printf("  ");

            if (i < mA && j < nA) printf("%3d ", *(A+(nA*i+j)));
            else if (i >= mA && j < nA) printf("    "); // 4 spaces since values are padded (4 spaces per value)
            else if (i < mB && j >= nA && j < (nA+nB)) printf("%3d ", *(B+(nB*i+(j-nA))));
            else if (i >= mB && j >= nA && j < (nA+nB)) printf("    ");
            else if (i < mA && j >= (nB+nA)) printf("%3d ", *(C+(nB*i+(j-nA-nB))));  
            else if (i >= mA && j >= (nB+nA)) printf("    ");
        }
        printf("\n");
    }
    printf("\n");  
}

int main (int argc, char **argv)
{
    int n = 0;
    if (argc > 1) n = atoi(argv[1]);

    
    int mA = 5; // number of rows of A
    int nA = 4; // number of columns of A
    int *A = (int*) malloc(mA * nA * sizeof(int)); // allocate space for array in memory

    for (int i=0; i<mA*nA; i++) *(A+i) = i; // == A[i]

    int mB = 4;
    int nB = 5;
    int B[mB*nB]; // same functionality as malloc in this case

    for (int i=0; i<mB*nB; i++) B[i] = i; // == *(B+i)

    int C[mA*nB];

    // multiply the matrices
    for (int i=0; i<mA; i++) // rows of A
    {
        for (int j=0; j<nB; j++) // columns of B
        {
            int val = 0;

            for (int k=0; k<nA; k++) // columns of A and rows of B (have to be equal)
            {
                val += A[nA*i+k] * B[nB*k+j];
            }

            C[nB*i+j] = val;
        }
    }

    print_matrix_product(A, B, C, mA, nA, mB, nB);
    
    return 0;
}