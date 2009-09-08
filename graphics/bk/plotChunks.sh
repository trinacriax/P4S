#!/bin/bash
#file.dat da analizzare
filedat=`basename $1`
#
#numero di chunks da analizzare da
if [ $# -le 4 ];
then
        echo "Usage `basename $0` file.dat from_chunk to_chunk step_chunk DEBUG"
        exit
fi
if [ $# -eq 5 ];
then
        DEBUG=$5
else
        DEBUG=0
fi
step=$4
startc=$2
let startc=$startc*2
let startc=$startc+1
# al 
endc=$3
let endc=$endc+1
let endc=$endc*2;
let step=$step*2
let ever=$endc/20
`egrep "# " $1> cfgfile`
rows=`wc -l cfgfile|egrep -o "[0-9]+"`
if [ $DEBUG -eq "1" ]
	then
	echo "ROWS $rows"
fi

        exec<"cfgfile"
        while read line; 
        do 
                if [ $DEBUG -eq "1" ]
        then
        echo $line
        fi
                cid=`echo $line | egrep -o "# [0-9]+ #"|egrep -o [0-9]+`
                if [ $DEBUG -eq "1" ]
        then
        echo "ID = $cid"
        fi
                configz=`echo $line | egrep -o "Config [0-9]+"|egrep -o [0-9]+`
                if [ $DEBUG -eq "1" ]
        then
        echo "config = $configz"
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
	
	echo "Title plot "$title        
        echo "Config " $cid
        fi
        outf="plotId"$configz".gnu"
        oute="plotId"$configz".eps"
        echo "Outgnu $outf"
        echo "Outeps $oute"     
        #color font \"Tahoma,14\"" > $outf
        echo "set terminal postscript eps enhanced color" > $outf
        echo "set output \"$oute\"">>$outf
        #echo "set xrange [0:8]">>$outf
        for nn in `seq 1 100`;
        do 
		echo "set style line $nn linetype $nn pt $nn linewidth 3" >> $outf
        done
        pn=0;
	echo "set key left">>$outf
        echo "set title \"$title\"">>$outf      
	echo "set title \"$title\"">>$outf
	let cid=$cid*2	
	 for arow in `seq $startc $step $endc`
        do
                #curve title is the chunk number
                let tit=$arow+1
                let tit=$tit/2
                let tit=$tit-1
                let pn=$pn+1
                acapo=',\';             
                if [ $arow -eq $startc ]; 
                        then 
                        echo "plot \\" >> $outf; 
                fi              
                let brow=$arow+1
                if [ $brow -eq $endc ];
                        then
                        acapo=''
                fi
                let taz=$brow+$step
                if [ $taz -gt $endc ];
                        then
                        acapo=''
                fi
                echo "\"$filedat\" index $cid u (\$$arow/1000):$brow every $ever t \"$tit\" w lp ls $pn$acapo" >> $outf
	done
done < "cfgfile"        
for outz in `ls -1 *.gnu`;
do
	gnuplot $outz
done
