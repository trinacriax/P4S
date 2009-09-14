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
rows=`cat cfgfile|wc -l`
if [ $DEBUG -eq "1" ]
	then
	echo "ROWS $rows"
fi
if [ $DEBUG -eq "1" ]
	then
	echo "Creating $NAMEF.1 from $NAMEF "
fi

cat $NAMET>$NAMEF
`cp $NAMEF $NAMEF.1`
sed "s/CCOUTFILE/$outeps/g" $NAMEF.1 > $NAMEF
rm $NAMEF.1
echo >>$NAMEF
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
cpid=0
# Size 999, Chunks 5000, Exps 1, ChunkRate 1000, DegreeK 16, 
# BMS 1.0, SUP 1000000, SDW 10000000, 
# BMP 1.5 , BUP 1500000 , BDW 15000000 , 
# BDS 1.0 , 
# ActiveUp 1, ActiveDw 1, PassiveUp 1, PassiveDw 10, PushWin 4, PullWin 4, PushRetry 1, PullRetry 1,
# DelayType 0, Select 1, Mindelay 10, Maxdelay 250

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
	csize=`echo $aline | egrep -o "Size [0-9]+"[,]*|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "SIZE = $csize"
	fi
	crate=`echo $aline | egrep -o "ChunkRate [0-9]+"[,]*|egrep -o [0-9]+`
	crate=`echo "scale=1;$crate/1000"|bc`
	if [ $DEBUG -eq "1" ]
		then
		echo "crate= $crate"
		#crate=`echo "scale=1;$crate/1000"|bc`
		#echo $crate
	fi
	if [[ $crate < 1 ]]
	then
		crate="0"$crate
	fi
	cbupmult=`echo $aline | egrep -o "BMP[ ]+[0-9]+[.]*[0-9]{1}"[,]*|egrep -o [.0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "bupmult = $cbupmult"
	fi
	cdelay=`echo $aline | egrep -o "Delay [0-9]+"[,]*|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cdelay = $cdelay"
	fi
	cselect=`echo $aline | egrep -o "Select [0-9]"|egrep -o [0-9]`
	if [ $DEBUG -eq "1" ]
		then
		echo "cselect = $cselect"
	fi
	cmindelay=`echo $aline | egrep -o "Mindelay [0-9]+[,]*"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cmindelay = $cmindelay"
	fi
	cmaxdelay=`echo $aline | egrep -o "Maxdelay [0-9]+[,]*"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cmaxdelay = $cmaxdelay "
	fi
	comega=`echo $aline | egrep -o "PushWin [0-9]+[,]*"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "comega = $comega "
	fi
	caup=`echo $aline | egrep -o "ActiveUp [0-9]+[,]*"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cpiup = $caup "
	fi
	cpiup=`echo $aline | egrep -o "PassiveUp [0-9]+[,]*"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
		then
		echo "cpiup = $cpiup "
	fi
	sselect="R-"
	sdelay="U"	
	if [[ $cdelay = 1 ]]
		then 
		sdelay="G"
	fi
	if [[ $cdelay = 2 ]]
		then 
		sdelay="E"
	fi
	if [ $cselect -eq "1" ]	
		then
		sselect="R-"
	elif [ $cselect -eq "2" ]
		then
		sselect="D-"
	fi
	post=",\\"	
	sdelay=$sselect""$sdelay"["$cmindelay","$cmaxdelay"]"
	title="$cbupmult""Bs,Ts=""$crate""s,$sdelay,\\{\\/Symbol w\\}=$comega,\\{\\/Symbol a\\}=$caup"
	#,\\{\\/Symbol p\\}=$cpiup"
	if [ $DEBUG -eq "1" ]
		then
		echo $title
	fi	
	let csize=$csize+1
	if [ $DEBUG -eq "1" ]
	then
	echo "Copy $NAMER to $NAMER.old"
	fi
	#echo $NAMER
	`cp $NAMER $NAMER.old`
	rm -f $NAMER.1	
	let cpid=$cpid+1
	while read anotherline; 
		do		
		if [ ${#anotherline} -gt 0 ]
		then		
			echo $anotherline| sed "s/CPID/$cpid/g" >> $NAMER.1
		fi
	done<"$NAMER.old"
	rwz=`cat $NAMER.old|wc -l`	
	if [ $rwz -eq "0" ]
	then
		sed "s/CPID/$cpid/g" $NAMER.old >> $NAMER.1
	#	let cpid=$cpid+1
	fi
	`cp $NAMER.1 $NAMER.old`
	sed "s/CCFILE/$pfile/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`
	sed "s/CCSIZE/$csize/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`	
	sed "s/CCID/$cid/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`	
	sed "s/CCTITLE/`echo $title`/g" $NAMER.old > $NAMER.1	
	`cp $NAMER.1 $NAMER.old`
	stz=""
	#echo "Rows " $rows " stz="$stz"="
	#cat $NAMER.old
	if [ $rows -gt "1" ]
		then
		let rows=$rows-1		
		stz=",\\\\"
	fi
	#echo $NAMER.old
	sed "s/ EEOL/$stz/g" $NAMER.old > $NAMER.1	
	#cat $NAMER.old
	if [[ $rwz = 0 ]]
	then
		echo >> $NAMER.1
	fi
	cat $NAMER.1 >> $NAMEF
	rm -f $NAMER.1
	rm -f $NAMER.old
	if [ $DEBUG -gt "1" ]
		then
		echo " >>>>>>>>>>>>>$NAMEF>>>>>>>>>>>>>"
		cat $NAMEF
		echo " <<<<<<<<<<<<<<<<<<<<<<<<<<"
	fi
done<"cfgfile"
rm -f *.1
rm -f *.old
echo "Finish to do "$NAMEF
