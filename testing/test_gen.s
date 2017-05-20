	.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"
	.text
main:
	li	$t0,	8
	sw	$t0,	-8($gp)
	lw	$t0,	-8($gp)
	lw	$t1,	-4($gp)
	add	$t0,	$t0,	$t1
	sw	$t0,	-12($gp)
	lw	$t0,	-8($gp)
	li	$t1,	2
	div	$t0,	$t0,	$t1
	sw	$t0,	-12($gp)
	lw	$t0,	-12($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$t1,	9
	sw	$t1,	-12($gp)
	lw	$t1,	-12($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t1
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$s0,	0x1
	sw	$s0,	-16($gp)
	lw	$s0,	-16($gp)
	#Escriure
	beqz	$s0,	eti0
	li	$v0,	4
	la	$a0,	ecert
	b	eti1
	eti0:
	li	$v0,	4
	la	$a0,	efals
	eti1:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$t2,	0x0
	sw	$t2,	-16($gp)
	lw	$t2,	-16($gp)
	#Escriure
	beqz	$t2,	eti2
	li	$v0,	4
	la	$a0,	ecert
	b	eti3
	eti2:
	li	$v0,	4
	la	$a0,	efals
	eti3:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	lw	$s1,	-0($gp)
	sw	$s1,	-420($gp)
	jr $ra
