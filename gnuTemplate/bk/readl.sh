#!/bin/bash
echo $1
exec<"$1"
while read cline; do		
		echo $cline
		#sed "s/CPID/$cpid/g" $bline >> $NAMER.1
		#let cpid=$cpid+1
	done