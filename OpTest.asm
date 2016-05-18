reset:
    ldi r0, 0
    ldi r1, 1

main:
    input_loop:
        in r2, 3
        tst r2
        breq input_loop

    out 3, r2
    rjmp input_loop
hlt
