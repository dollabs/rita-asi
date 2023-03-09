#!/usr/bin/env bash

files=$(ls *.log)
for f in $files; do
  begin=$(head -1 "$f" | cut -f 2 -d',')
  end=$(tail -1 "$f" | cut -f 2 -d',')
  duration=$(((end - begin)/1000))
  count=$(wc -l "$f")
  name=$(basename "$f" .log)
#  name="$name.metadata"
#  nc=$(wc -l "$name")
  echo "####"
#  echo "$nc   $name"
  echo "$count"
  echo "Duration: $duration (secs)"
  echo
done
