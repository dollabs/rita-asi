#!/usr/bin/env bash

## To replace spaces with _ in files names.

IFS=$'\n'
files=$(ls *.metadata)
#echo $files

for f in $files; do
  ## Replace space with `_`
  #  echo "$f"
  #  new_f=$(sed 's/ /_/g' <<< "$f")
  new_f=${f/ /_}
  if [[ "$f" != "$new_f" ]]; then
    echo "$new_f <== $f"
    #     echo "not equal"
    mv "$f" "$new_f"
  fi

done
