#!/bin/bash

LICENSE_FILE=
attach_license() #file list
{
    for file in $*; do
        echo "attach [$LICENSE_FILE] to [$file]"
        if [ "$file" = "$LICENSE_FILE" ]; then
            echo "[skip] source equals target"
        else
            if [ ${!SIMULATE_EXECUTION[@]} ]; then
                echo "this is a simulation"
            else
                cat $LICENSE_FILE $file > tmp.attach
                mv  tmp.attach $file
            fi
        fi
    done
    exit 0
}

helper()
{
    echo -e "usage: $0 [options] files... "
    echo -e " Options:"
    echo -e "\t-l <text file>    file to attach to files-list"
    echo -e "\t-s                simulate execution (must be first argument)"
    echo -e "\t-h, --help        this help"
    exit 1
}


if [ $# -eq 0 ]; then
    helper
fi

# parameter parsing
while [ $# -gt 0 ]; do
    case $1 in
        "-l")
            # read configuration file
            shift #shift parameter
            LICENSE_FILE=$1
            shift #file list
            attach_license $*
            ;;
        "-s")
            SIMULATE_EXECUTION=
            ;;
        "--help"|"-h"|*)
            helper
            ;;
    esac
    shift
done
