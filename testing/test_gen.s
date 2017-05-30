	
.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"

.text
main:
	move	$fp,	$sp
	#Escriure
	
.data
	_eti0: .asciiz "Escriu: "
	
.text
	li	$v0,	4
	la	$a0,	_eti0
	syscall
	#read
	li	$v0,	5
	syscall
	move	$t0,	$v0
	sw	$t0,	-36($sp)
	#Escriure
	
.data
	_eti1: .asciiz "Esto es hola = "
	
.text
	li	$v0,	4
	la	$a0,	_eti1
	syscall
	lw	$t1,	-36($sp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t1
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	lw	$s0,	-0($sp)
	#NOT
	not	$s0,	$s0
	andi	$s0,	$s0,	 0x00000001
	#Escriure
	
.data
	_eti2: .asciiz "Escriu: "
	
.text
	li	$v0,	4
	la	$a0,	_eti2
	syscall
	#read
	li	$v0,	5
	syscall
	move	$t2,	$v0
	sw	$t2,	-16($gp)
	#Escriure
	
.data
	_eti3: .asciiz "Esto es hola = "
	
.text
	li	$v0,	4
	la	$a0,	_eti3
	syscall
	lw	$s1,	-16($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$s1
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr	$ra
