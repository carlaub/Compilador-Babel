	
.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"

.text
main:
	move	$fp,	$sp
	lw	$t0,	-0($gp)
	#NOT
	not	$t0,	$t0
	andi	$t0,	$t0,	 0x00000001
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
	move	$t1,	$v0
	sw	$t1,	-16($gp)
	#Escriure
	
.data
	_eti1: .asciiz "Esto es hola = "
	
.text
	li	$v0,	4
	la	$a0,	_eti1
	syscall
	lw	$s0,	-16($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$s0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr	$ra
