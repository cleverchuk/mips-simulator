# This code calculates the arithmetic mean of the array
.data
array: .double 1.0, 2.0, 3.0, 4.0, 5.0, 6.0  # Array of floating-point numbers
len: .word 6                              # Length of the array

.text
mtc1 $zero, $f12     # Set $f12 to 0.0 (initialize the sum to 0.0)
mthc1 $zero, $f12    # Set $f12 to 0.0 (initialize the sum to 0.0)

la $t4, array          # Load the address of the array into $t4
li $t2, 48           # Load the index of the last element of the array (6 elements * 8 bytes per double = 48 bytes)

loop:
bnez $t2, 1          # If $t2 is not zero, continue to the next iteration of the loop

j end                # Jump to the end of the program if $t2 is zero
addi $t2, $t2, -8    # Decrement $t2 by 8 (move to the previous element)
add $t3, $t4, $t2    # Calculate the address of the current element of the array

ldc1 $f10, 0($t3)    # Load the value of the current element into $f10
add.d $f12, $f12, $f10  # Add the value of the current element to the sum in $f12
j loop               # Jump back to the beginning of the loop

end:
la $t4, len          # Load the address of the length variable into $t4
lwc1 $f10, 0($t4)   # Load the value of the length into $f10

cvt.d.w $f10, $f10   # Convert the integer length to double precision
div.d $f12, $f12, $f10  # Divide the sum by the length to calculate the mean
li $v0, 3            # Set $v0 to 3 (system call code for printing a floating-point number)
syscall

li $a0, 10           # Set $a0 to 10 (ASCII code for newline character)
li $v0, 11           # Set $v0 to 11 (system call code for printing a character)
syscall


#============================================================================================
#### This code computes the geometric mean of the array ####
.data
array: .double 1, 2, 3, 4, 5, 6    # Array of numbers
length: .word 6                  # Length of the array
eps:    .double 0.001

.text
.globl main

main:
# Initialize variables
la $t0, array                # Load the address of the array into $t0
la $t1, length               # Load the address of length into $t1
lw $t1, 0($t1)               # Load the value of length into $t1
li $t2, 1                    # Initialize $t2 with 1

mtc1 $t2, $f1                # Move $t2 to the floating-point register $f1
cvt.d.w $f1, $f1             # Convert the integer value in $f1 to double precision

loop:
beqz $t1, end_loop           # If length is zero, exit the loop
ldc1 $f2, 0($t0)             # Load the double value at the current index of array into $f2
mul.d $f1, $f1, $f2          # Multiply the product with the current element and store the result in $f1
addi $t0, $t0, 8             # Increment the array pointer by 8 bytes (64 bits)
addi $t1, $t1, -1            # Decrement the length by 1
j loop                      # Jump back to the loop

end_loop:
addi $sp, $sp, -16           # Allocate space on the stack
la $t1, length               # Load the address of length into $t1
lw $t1, 0($t1)               # Load the value of length into $t1

sdc1 $f1, 0($sp)             # Store the product value on the stack
sw $t1, 8($sp)               # Store the length value on the stack
jal binary_search            # Jump and link to the binary_search function

# Print the result
li $v0, 3                    # System call code for printing float
syscall

# Print the line
li $v0, 11                   # System call code for printing character
li $a0, 10                   # Load the ASCII code for a new line into $a0
syscall

# Exit the program
li $v0, 10                   # System call code for exit
syscall

# Functions
pow_n:
ldc1 $f20, 0($sp)            # Load the number from the stack
ldc1 $f21, 0($sp)            # Load the number from the stack
lw $t0, 8($sp)               # Load n from the stack

pow_n_loop:
addi $t0, $t0, -1            # Decrement n by 1
bnez $t0, pow_n_continue     # If n is not zero, continue
mov.d $f12, $f21             # Move the result (ans) to the output register
jr $ra                       # Return

pow_n_continue:
mul.d $f21, $f21, $f20       # Multiply ans by the number
j pow_n_loop                 # Jump back to pow_n_loop

binary_search:
ldc1 $f1, 0($sp)             # Load the number from the stack
ldc1 $f10, 0($sp)            # Load the number from the stack
lw $t0, 8($sp)               # Load n from the stack
li $t2, 1                    # Initialize low = 1.0

mtc1 $t2, $f2                # Move $t2 to the floating-point register $f2
cvt.d.w $f2, $f2             # Convert the integer value in $f2 to double precision
la $t2, eps                  # Load the address of eps into $t2

ldc1 $f3, 0($t2)             # Load the value of eps into $f3
li $t3, 2                    # Load 2 into $t3
mtc1 $t3, $f8                # Move $t3 to the floating-point register $f8
cvt.d.w $f8, $f8             # Convert the integer value in $f8 to double precision

bs_loop:
sub.d $f4, $f1, $f2          # Calculate high - low and store the result in $f4
cmp.le.d $f5, $f4, $f3       # Compare (high - low) with eps and set the result in $f5
bc1eqz $f5, 1                # If (high - low) <= eps, exit the loop
j bs_exit                    # Jump to bs_exit

add.d $f6, $f2, $f1          # Calculate low + high and store the result in $f6
div.d $f7, $f6, $f8          # Calculate (low + high) / 2.0 and store the result in $f7
addi $sp, $sp, -52           # Allocate space on the stack

# Preserve registers
sw $ra, 48($sp)
sdc1 $f10, 40($sp)           # Push low onto the stack
sdc1 $f2, 32($sp)            # Push low onto the stack
sdc1 $f1, 24($sp)            # Push high onto the stack

sdc1 $f8, 16($sp)            # Push 2.0 onto the stack
sw $t0, 8($sp)               # Push n onto the stack
sdc1 $f7, 0($sp)             # Push mid onto the stack

jal pow_n                    # Call the pow_n function to calculate pow_n(mid, n)

# Restore registers
lw $ra, 48($sp)
ldc1 $f10, 40($sp)           # Pop low from the stack
ldc1 $f2, 32($sp)            # Pop low from the stack
ldc1 $f1, 24($sp)            # Pop high from the stack

ldc1 $f8, 16($sp)            # Pop 2.0 from the stack
lw $t0, 8($sp)               # Pop n from the stack
ldc1 $f7, 0($sp)             # Pop mid from the stack
addi $sp, $sp, 52            # Deallocate space on the stack

cmp.lt.d $f9, $f12, $f10     # Compare pow_n(mid, n) with high and set the result in $f9
bc1eqz $f9, 1                # If pow_n(mid, n) < high, jump to bs_low
j bs_low                     # Jump to bs_low
mov.d $f1, $f7               # Set high = mid
j bs_loop                    # Jump back to bs_loop

bs_low:
mov.d $f2, $f7               # Set low = mid
j bs_loop                    # Jump back to bs_loop

bs_exit:
mov.d $f12, $f2              # Set low = mid
jr $ra                       # Return


#============================================================================================
#### This code computes the hypothenus given x and y ####
.text
.globl main

main:
# Load default values for side1 and side2
li $s0, 3        # Load 3 into $s0
li $s1, 4        # Load 4 into $s1

# Calculate the squares of side1 and side2
mul $t0, $s0, $s0    # side1 * side1
mul $t1, $s1, $s1    # side2 * side2

# Calculate the sum of the squares
add $t2, $t0, $t1    # side1^2 + side2^2

# Calculate the square root of the sum
mtc1 $t2, $f12
cvt.s.w $f12, $f12
sqrt.s $f12, $f12      # Square root of the sum

# Print the result
li $v0, 2            # System call code for printing integer
syscall

# Print the line
li $v0, 11          # System call code for printing character
li $a0, 10      # load new line ascii code to $a0
syscall

# Exit the program
li $v0, 10           # System call code for exit
syscall

#=============================================================================================
#### This code calculates x to the power ####
.text
.globl main
main:
# Store x and n in registers
li $s0, 2         # Move x to $s0
li $s1, 2         # Move n to $s1

# Initialize result to 1
li $t0, 1             # Load 1 into $t0

loop:
beqz $s1, exit     # Exit loop if n == 0
mul $t0, $t0, $s0  # Multiply result by x
addi $s1, $s1, -1    # Decrement n by 1
j loop             # Jump to loop label

exit:
# Print the result
li $v0, 1          # System call code for printing integer
move $a0, $t0      # Move result to $a0
syscall

# Print the line
li $v0, 11          # System call code for printing character
li $a0, 10      # load new line ascii code to $a0
syscall

# Exit the program
li $v0, 10         # System call code for exit
syscall

#============================================================================================
#### This snippet computes the root of the function y = x^2 - x using Newton approximation ####
.data
x0: .float 1.5       # Initial guess for the root
epsilon: .float 0.001 # Error tolerance

.text
.globl main

main:
# Load initial guess and error tolerance
la $t0, x0
lwc1 $f0, 0($t0)         # Load x0 into $f0
la $t0, epsilon
lwc1 $f1, 0($t0)   # Load epsilon into $f1

loop:
# Compute the function value and its derivative at x0
mul.s $f2, $f0, $f0     # Compute x0^2
sub.s $f3, $f2, $f0     # Compute x0^2 - x0 (function value)
addi $sp, $sp, -4      # Allocate space on the stack
swc1 $f0, 0($sp)       # Store x0 on the stack
addi $sp, $sp, -4      # Allocate space on the stack
swc1 $f2, 0($sp)       # Store x0^2 on the stack
addi $sp, $sp, -4      # Allocate space on the stack
swc1 $f3, 0($sp)       # Store function value on the stack
jal calculate_derivative # Jump to calculate_derivative function
addi $sp, $sp, 12      # Deallocate the stack space

# Calculate the new approximation using Newton's method
div.s $f6, $f3, $f6   # Divide function value by derivative
sub.s $f0, $f0, $f6   # Subtract the division result from x0

# Check if the approximation is within the error tolerance
abs.s $f12, $f6        # Take the absolute value of the division result
cmp.lt.s $f12, $f12, $f1   # Compare the absolute value with epsilon
bc1eqz $f12, 1             # Branch if true to the exit label
j exit

# Repeat the loop
j loop

calculate_derivative:
lwc1 $f4, 8($sp)       # Load x0 from the stack
lwc1 $f5, 4($sp)       # Load x0^2 from the stack
li $t4, 2
mtc1 $t4, $f12
cvt.s.w $f12, $f12
mul.s $f6, $f4, $f12   # Compute 2 * x0
li $t4, 1
mtc1 $t4, $f12
cvt.s.w $f12, $f12
sub.s $f6, $f6, $f12   # Compute 2 * x0  - 1 (derivative value)
jr $ra

exit:
# Print the result
li $v0, 2             # System call code for printing float
mov.s $f12, $f0     # Load the result from memory to $f12
syscall

# Print the line
li $v0, 11          # System call code for printing character
li $a0, 10      # load new line ascii code to $a0
syscall

# Exit the program
li $v0, 10            # System call code for exit
syscall
