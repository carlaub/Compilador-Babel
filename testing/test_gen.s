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
	lw	$t0,	-0($gp)
	sw	$t0,	-416($gp)
