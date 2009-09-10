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
target=$2
outeps=$3
DEBUG=0
if [ $# -eq "4" ]
then
	echo "DEBUG ACTIVE"
	DEBUG=1
fi
if [ $DEBUG -eq "1" ]
then
	echo "TARGET $target"
fi
NAMET=$target"_file_template"
NAMEF=$target".gnu"
rm -f $NAMEF
NAMER=$target"_row_template"
if [ $DEBUG -eq "1" ]
	then
	echo "File $pfile Dir $pdir ;"
fi
#reading configuration file
`egrep "# [0-9]+ #" $pdir/$pfile > cfgfile`
rows=`wc -l cfgfile|egrep -o "[0-9]+"`
if [ $DEBUG -eq "1" ]
	then
	echo "ROWS $rows"
fi
cat $NAMET>$NAMEF
`cp $NAMEF $NAMEF.1`
sed "s/CCOUTFILE/$outeps/g" $NAMEF.1 > $NAMEF
rm $NAMEF.1
echo >>$NAMEF
for nn in `seq 1 35`;
do 
	echo "set style line $nn linetype $nn pt $nn linewidth 3" >> $NAMEF
done
echo "set mytics 5" >> $NAMEF
echo "set mxtics 4" >> $NAMEF
echo "set xtics nomirror" >> $NAMEF
echo "set key out horiz top center" >> $NAMEF
echo "set size 1,1" >> $NAMEF
echo "plot \\" >> $NAMEF
cpid=0
while read aline; 
	do
	if [ $DEBUG -eq "1" ]
		then
		echo "CFG LINE $aline"
	fi
	cid=`echo $aline | egrep -o "# [0-9]+ #"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "ID = $cid"
	fi
	csize=`echo $aline | egrep -o "Size [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "SIZE = $csize"
	fi
	crate=`echo $aline | egrep -o "ChunkRate [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "crate= $crate"
	fi
	cbupmult=`echo $aline | egrep -o "BUMULT [.0-9]+,"|egrep -o [.0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "bupmult = $cbupmult"
	fi
	cdelay=`echo $aline | egrep -o "Delay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cdelay = $cdelay"
	fi
	cselect=`echo $aline | egrep -o "Select [0-9]"|egrep -o [0-9]`
	if [ $DEBUG -eq "1" ]
		then
		echo "cselect = $cselect"
	fi
	cmindelay=`echo $aline | egrep -o "Mindelay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cmindelay = $cmindelay"
	fi
	cmaxdelay=`echo $aline | egrep -o "Maxdelay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cmaxdelay = $cmaxdelay "
	fi
	sselect="Rnd"
	sdelay="Unif"	
	if [[ $cdelay = 1 ]]
		then 
		sdelay="Gaus"
	fi
	if [[ $cdelay = 2 ]]
		then 
		sdelay="ExpTr"
	fi
	if [[ $cselect = 1 ]]
		then 
		sselect="Del"
	fi
	post=",\\"	
	sdelay=$sselect""$sdelay"["$cmindelay","$cmaxdelay"]"
	title="$cbupmult""Bs,Ts=""$crate""ms,$sdelay"
	if [ $DEBUG -eq "1" ]
		then
		echo $title
	fi	
	let csize=$csize+1
	`cp $NAMER $NAMER.old`
	rm -f $NAMER.1
	let cpid=$cpid+1
	while read anotherline; 
		do		
		if [ ${#anotherline} -gt 0 ]
		then		
			echo $anotherline >> $NAMER.1
		fi
	done<"$NAMER.old"
		`cp $NAMER.1 $NAMER.old`
		sed "s/CCFILE/$pfile/g" $NAMER.old > $NAMER.1
		`cp $NAMER.1 $NAMER.old`
		sed "s/CCSIZE/$csize/g" $NAMER.old > $NAMER.1
		`cp $NAMER.1 $NAMER.old`	
		sed "s/CCID/$cid/g" $NAMER.old > $NAMER.1
		`cp $NAMER.1 $NAMER.old`	
		sed "s/CCTITLE/`echo $title`/g" $NAMER.old > $NAMER.1	
		`cp $NAMER.1 $NAMER.old`	
		sed "s/CPID/$cpid/g" $NAMER.old > $NAMER.1	
		`cp $NAMER.1 $NAMER.old`
		stz=""
		if [[ $rows > 1 ]]
			then
			let rows=$rows-1		
			stz=",\\\\"			
		fi
		sed "s/ EEOL/$stz/g" $NAMER.old > $NAMER.1	
		if [[ $rwz = 0 ]]
		then
			echo >> $NAMER.1
		fi
		cat $NAMER.1 >> $NAMEF	
	rm -f $NAMER.1
	rm -f $NAMER.old
	if [ $DEBUG -eq "1" ]
		then
		echo " >>>>>>>>>>>>>$NAMEF>>>>>>>>>>>>>"
		cat $NAMEF
		echo " <<<<<<<<<<<<<<<<<<<<<<<<<<"
	fi
done<"cfgfile"
rm -f *.1
rm -f *.old
echo "Finish to do "$NAMEF
