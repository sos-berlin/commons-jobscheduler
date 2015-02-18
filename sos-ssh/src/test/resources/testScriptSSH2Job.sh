#! /bin/bash
echo ---------------test script file tramsmission and execution via ssh-----------------
set e
ls
cc=$?
if [ $cc -ne 0 ]; then
    echo "ls failed with exit code $cc"
    exit $cc
fi
echo ---------------test script file tramsmission and execution complete----------------
