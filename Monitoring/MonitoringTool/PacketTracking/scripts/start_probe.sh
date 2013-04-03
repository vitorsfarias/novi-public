#!/bin/bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/novi_novi/pt/packet/libipfix/libmisc
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/novi_novi/pt/packet/libipfix/lib
nohup /home/novi_novi/pt/packet/impd4e/impd4e $* >/dev/null 2>&1 < /dev/null &
