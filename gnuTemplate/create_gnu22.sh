#!/bin/bash
E_BADARGS=65
case $# in 
0)             # The vertical bar means "or" in this context.
echo "Usage: `basename $0` path-to-file.dat target(.gnu) outfile(.eps)"
exit $E_BADARGS  # If 0 or 1 arg, then bail out.
;;
1)
echo "Usage: `basename $0` path-to-file.dat target(.gnu) outfile(.eps)"
exit $E_BADARGS  # If 0 or 1 arg, then bail out.
;;
2)
echo "Usage: `basename $0` path-to-file.dat target(.gnu) outfile(.eps)"
exit $E_BADARGS  # If 0 or 1 arg, then bail out.
;;
esac
pdir=`dirname $1`
pfile=`basename $1`
DEBUG=0
if [ $# -eq "4" ]
then
	echo "DEBUG ACTIVE"
	DEBUG=1
fi
target=$2
NAMET=$target"_file_template"
NAMEF=$target".gnu"
rm -f $NAMEF
NAMER=$target"_row_template"
rows=`egrep "#" $1|wc -l`
if [ $DEBUG -eq "1" ]
	then
	echo "File $pfile Dir $pdir ;"
fi
if [ $DEBUG -eq "1" ]
then	
	echo "Out in $NAMEF"
	echo "Template body $NAMET"
	echo "Tempalte rows $NAMER"
	echo "ROWS $rows"
	echo "Outeps is $3"
fi
cat $NAMET>$NAMEF
`cp $NAMEF $NAMEF.1`
sed "s/CCOUTFILE/$3/g" $NAMEF.1 > $NAMEF
rm $NAMEF.1
for nn in `seq 1 $rows`;
do 
	echo "set style line $nn linetype $nn pt $nn linewidth 3" >> $NAMEF
done
echo "set mytics 5" >> $NAMEF
echo "set mxtics 4" >> $NAMEF
echo "set xtics nomirror" >> $NAMEF
echo "set key out horiz top center" >> $NAMEF
echo "set size 1,1" >> $NAMEF
echo "plot \\" >> $NAMEF
title=""
counter=0
block=0
blockc=0
while read aline
	do
	#echo $aline
	if [ ${#aline} -gt 0 ]
		then
		if [ ${#title} -eq 0 ]
		then
			title=$aline
			echo "Title is $title"
			let counter=$counter+1			
		elif [ $blockc -eq 0 ]
		then
			#echo "row is $aline"
			echo $NAMER			
			`cp $NAMER $NAMER.old`
			while read bline
			do		
			#$echo "LINE $bline"
			if [ ${#bline} -gt 0 ]
			then				
				sed "s/CCFILE/$pfile/g" $NAMER.old > $NAMER.1
				`cp $NAMER.1 $NAMER.old`
				sed "s/CCID/$block/g" $NAMER.old > $NAMER.1
				`cp $NAMER.1 $NAMER.old`
				sed "s/CCTITLE/`echo $title`/g" $NAMER.old > $NAMER.1	
				`cp $NAMER.1 $NAMER.old`				
				sed "s/CCLINE/$counter/g" $NAMER.old > $NAMER.1
				`cp $NAMER.1 $NAMER.old`
				if [ $counter -eq $rows ] 
				then
					sed "s/EEOL//g" $NAMER.old > $NAMER.1
					`cp $NAMER.1 $NAMER.old`					
				else
					sed "s/EEOL/,\\\\/g" $NAMER.old > $NAMER.1
					`cp $NAMER.1 $NAMER.old`
				fi
				#cat $NAMER.oldq
			fi
			done<"$2_row_template"			
			blockc=10			
			cat $NAMER.old >> $NAMEF
		fi
	else		
		if [ $blockc -gt 0 ]
		then			
			let block=$block+1
			echo "new block $block "
			blockc=0
			title=""
		fi
	fi
done<"$1"

echo "Finish to do "$NAMEF
