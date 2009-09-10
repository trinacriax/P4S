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
NAMET=$target"_file_template.gnu"
NAMEF=$target".gnu"
rm -f $NAMEF
NAMER=$target"_row_template.gnu"
if [ $DEBUG -eq "1" ]
then
echo "File $pfile Dir $pdir ;"
fi
#reading configuration file
`egrep "# " $pdir/$pfile > cfgfile`
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
exec<"cfgfile"
while read line; do 
	if [ $DEBUG -eq "1" ]
then
echo $line
fi
	cid=`echo $line | egrep -o "# [0-9]+ #"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "ID = $cid"
fi
	csize=`echo $line | egrep -o "Size [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "SIZE = $csize"
fi
	crate=`echo $line | egrep -o "ChunkRate [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "crate= $crate"
fi
	cbupmult=`echo $line | egrep -o "BUMULT [.0-9]+,"|egrep -o [.0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "bupmult = $cbupmult"
fi
	cdelay=`echo $line | egrep -o "Delay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "cdelay = $cdelay"
fi
	cselect=`echo $line | egrep -o "Select [0-9]"|egrep -o [0-9]`
	if [ $DEBUG -eq "1" ]
then
echo "cselect = $cselect"
fi
	cmindelay=`echo $line | egrep -o "Mindelay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "cmindelay = $cmindelay"
fi
	cmaxdelay=`echo $line | egrep -o "Maxdelay [0-9]+,"|egrep -o [0-9]+`
	if [ $DEBUG -eq "1" ]
then
echo "cmaxdelay = $cmaxdelay "
fi
	sselect="Rnd"
	sdelay="Unif"
	if [[ $cselect = 1 ]]
		then sselect="Del"
	fi
	if [[ $cdelay = 1 ]]
		then sdelay="Gaus"
	elif [[ $cdelay -eq 2 ]]
		then sdelay="ExpTr"
	fi
	post=",\\"	
	sdelay=$sselect""$sdelay"["$cmindelay","$cmaxdelay"]"
	title="$cbupmult""Bs,Ts=""$crate""ms,$sdelay"
	if [ $DEBUG -eq "1" ]
then
echo $title
fi
	let "csize=$csize+1"
	`cp $NAMER $NAMER.old`
	sed "s/CCFILE/$pfile/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`
	sed "s/CCSIZE/$csize/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`
	sed "s/CCID/$cid/g" $NAMER.old > $NAMER.1
	`cp $NAMER.1 $NAMER.old`
	sed "s/CCTITLE/`echo $title`/g" $NAMER.old > $NAMER.1
	if [ $DEBUG -eq "1" ]
then
cat $NAMEF
fi
	cat $NAMER.1 >> $NAMEF
	if [[ $rows > 1 ]]
		then
		let "rows=$rows-1"
		echo ",\\" >>  $NAMEF
	fi
	if [ $DEBUG -eq "1" ]
then
cat $NAMEF
fi
done < "cfgfile"
rm *.1
rm *.old
echo "Finish to do "$NAMEF
