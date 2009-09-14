#!/bin/bash
CONFIGID=10100
echo -n "Network size to load "
read CCPEERS_N
for j in `seq 1 $CCPEERS_N` 
do
	echo -n "Network size [$j] "
	read CCPEERS[$j]
done
echo -n "Number of Chunks to load "
read CCCHUNKS_N
for j in `seq 1 $CCCHUNKS_N` 
do
	echo -n "Number of Chunks [$j] "	
	read CCCHUNKS[$j]	
done
echo -n "Neighbor size to load "
read CCDEGREE_N
for j in `seq 1 $CCDEGREE_N` 
do	echo -n "Neighbor size [$j] "
	read CCDEGREE[$j]
done
echo -n "RTT to load  "
read CCDRNG_N
for j in `seq 1 $CCDRNG_N` 
do
	echo -n "Min delay [$j] "
	read CCDMIN[$j]
	echo -n "Max delay [$j] "
	read CCDMAX[$j]
done
echo -n "Streaming rate Bs to load "
read CCBS_N
for j in `seq 1 $CCBS_N` 
do
	echo -n "Bs [$j] "
	read CCBS[$j]	
done
echo -n "Playout time to load "
read CCPLAYT_N
for j in `seq 1 $CCPLAYT_N`
do
        echo -n "PLAYT [$j] "
        read CCPLAYT[$j]
done	
echo -n "Pull rounds to load "
read CCPULLR_N
for j in `seq 1 $CCPULLR_N`
do
        echo -n "PULLROUND [$j] "
        read CCPULLR[$j]
done
echo -n "Chunks Time Ts to load "
read CCTS_N
for j in `seq 1 $CCTS_N` 
do
	echo -n "Chunks Time Ts [$j] "
	read CCTS[$j]	
done
echo -n "Bandiwdth to load "
read CCBMULT_N
let "CCBMULT_N=$CCBMULT_N+1"
echo -n "Bw Step is: "
read CCBSTEP
old=0;
CCBMULT[1]=`echo "scale=2; $CCBSTEP/2"|bc`
echo "Bandwidth multi [1] ${CCBMULT[1]}"
for j in `seq 2 $CCBMULT_N` 
do
	#echo "scale=2; $old+$CCBSTEP"
	val=`echo "scale=2; $old+$CCBSTEP"|bc`
	#echo "Value is $val"
	CCBMULT[$j]=$val
	echo "Bandwidth multi [$j] ${CCBMULT[$j]}"
	old=$val
done
echo -n "#Combination for connections to load "
read CCALPHAPI_N
for j in `seq 1 $CCALPHAPI_N` 
do
	echo -n "#Act up [$j] "
	read CCALPHAUP[$j]	
#done
#echo -n "#ActDw to load "
#read CCALPHADW_N
#for j in `seq 1 $CCALPHADW_N` 
#do
	echo -n "#Act dw [$j] "
	read CCALPHADW[$j]	
#done
#echo -n "#PasUp to load "
#read CCPIUP_N
#for j in `seq 1 $CCPIUP_N` 
#do
	echo -n "#Pas up [$j] "
	read CCPIUP[$j]	
#done
#echo -n "#PasDw "
#read CCPIDW_N
#for j in `seq 1 $CCPIDW_N` 
#do
	echo -n "#Pas dw [$j] "
	read CCPIDW[$j]	
done
echo -n "Omega Push and Pull to load "
read CCOMEGA_N
for j in `seq 1 $CCOMEGA_N` 
do
	echo -n "#Omega push [$j] "
	read CCOMEGAP1[$j]	
#done
#echo -n "Omega Pull to load "
#read CCOMEGAP2_N
#for j in `seq 1 $CCOMEGAP2_N` 
#do
	echo -n "#Omega pull [$j] "
	read CCOMEGAP2[$j]	
done
echo -n "#Push Attempts "
read CCRHOP1
echo -n "#Pull Attempts "
read CCRHOP2
echo -n "RTT delay type to load "
read CCDDELAY_N
for j in `seq 1 $CCDDELAY_N` 
do
	echo -n "#Delay type[$j] "
	read CCDDELAY[$j]
	if [ ${CCDDELAY[$j]} -gt 0 ]
		then
		echo -n "Mean delay "
		read CCMUDELAY
		echo -n "Deviation "
		read CCDEVDELAY
	fi
done
echo -n "Selection type "
read CCPPSEL_N
for j in `seq 1 $CCPPSEL_N` 
do
	echo -n "Selection type [$j] "
	read CCPPSEL[$j]
done
##
#echo "Net Size " ${#CCPEERS[*]}
for a in `seq 1 ${#CCPEERS[*]}` 
do
#	echo "Net Size " ${CCPEERS[$a]}
	for b in `seq 1 ${#CCCHUNKS[*]}` 
	do	
		#echo "Bandwidth multiplicator " ${#CCBMULT[*]}
		for q in `seq 1 ${#CCBMULT[*]}`
		do
			#echo "Chunk Size " ${#CCCHUNKS[*]}
			for c in `seq 1 ${#CCDEGREE[*]}`
			do
				#echo "DEGREE " ${#CCDEGREE[*]}
				for d in `seq 1 ${#CCDMIN[*]}` 
				do
					#echo "Delay Range [" ${#CCDMIN[*]}" : " ${#CCDMAX[*]}"]"
					for e in `seq 1 ${#CCBS[*]}` 
					do
						#echo "Bs " ${#CCBS[*]}
						for f in `seq 1 ${#CCTS[*]}`
						do
							#echo "Ts " ${CCTS[$f]}
							for ff in `seq 1 ${#CCPLAYT[*]}`
							do
							#echo "PLAYT " ${#CCPLAYT[*]}
							for af in `seq 1 ${#CCPULLR[*]}`
                                        	        do
							for g in `seq 1 ${#CCALPHAUP[*]}` 
							do
								h=g
								i=g
								l=g						
								#echo " AlphaUp " ${CCALPHAUP[$g]} ", AlphaDw " ${CCALPHADW[$h]} ", PiUp " ${CCPIUP[$i]} ", PiDw " ${CCPIDW[$l]}
								#for h in `seq 1 $CCALPHADW_N` 
								#do
								
									#echo "AlphaDw " ${CCALPHADW[$h]}
									#for i in `seq 1 $CCPIUP_N` 								
									#do								
										#echo "PiUp " ${CCPIUP[$i]}
										#for l in `seq 1 $CCPIDW_N` 									
										#do
											#l=g
											#echo "PiDw " ${CCPIDW[$l]}
											for m in `seq 1 ${#CCOMEGAP1[*]}` 
											do
											n=m
												#echo " Omega_Push " ${CCOMEGAP[$m]} ", Omega_Pull " ${CCOMEGAPP[$n]}
												#for n in `seq 1 $CCOMEGAPP_N` 
												#do											
													#echo "Omega_Pull " ${CCOMEGAPP[$n]}
													for o in `seq 1 ${#CCPPSEL[*]}` 
													do
														#echo "Sel " ${CCPPSEL[$o]}
														for p in `seq 1 ${#CCDDELAY[*]}` 
														do
															if [ ${CCDMAX[$d]} -lt ${CCTS[$f]} ]
															then
																let CONFIGID=$CONFIGID+1
																CCSEED=$RANDOM
																echo "INSIDE"
																TMP=0
																OUTF="config-p4s-0"$CONFIGID".txt"
																OUTT="config-p4s-0"$CONFIGID".tmp"
																#echo "Outfile is $OUTF"
																#echo "Out tmp is $OUTT"
																`cp config-p4s-template.txt  $OUTF`
																`cp $OUTF $OUTT`
																while [ $TMP -lt 4000000000000 ]
																do
																	let TMP=$CCSEED/8
																	let TMP=$TMP*$RANDOM
																	if [ $TMP -lt 4000000000000 ]
																	then
																		CCSEED=$TMP
																	fi
																done
																echo "Config file is " $OUTF
																echo  "Net Size "${CCPEERS[$a]}" Chunk Size "${CCCHUNKS[$b]}" DEGREE "${CCDEGREE[$c]}" Bs "${CCBS[$e]}" Ts "${CCTS[$f]}" Delay Range ["${CCDMIN[$d]}":"${CCDMAX[$d]}"] AlphaUp "${CCALPHAUP[$g]}", AlphaDw "${CCALPHADW[$h]}", PiUp "${CCPIUP[$i]}", PiDw "${CCPIDW[$l]}" Omega_Push "${CCOMEGAP1[$m]}", Omega_Pull "${CCOMEGAP2[$n]}", Delay "${CCDDELAY[$p]}" Sel "${CCPPSEL[$o]}" Seed is $CCSEED" 
																`sed "s/CCSEED/$CCSEED/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCPEERS/${CCPEERS[$a]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCCHUNKS/${CCCHUNKS[$b]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																dif=`echo "scale=2; ${CCTS[$f]}-${CCDMAX[$d]}"|bc`
																dif=`echo "scale=2; ${CCTS[$f]}/$dif"|bc`
																dif=`echo "scale=2; $dif+${CCBMULT[$q]}"|bc`																
																uno=`echo "scale=1; $dif"|bc`;
																due=`echo "scale=1; $dif"|bc|xargs printf "%1.1f"`;
																dif=`echo "scale=2; $due-$uno"|bc`;
																#echo "uno $uno due $due dif $dif";
																if [[ $dif > -1 ]]; 
																then 
																	dif=$due; 
																else 
																	dif=`echo "scale=2; 0.1+$due"|bc`
																fi
																`sed "s/CCBMULT/$dif/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCDEGREE/${CCDEGREE[$c]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCDMIN/${CCDMIN[$d]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCDMAX/${CCDMAX[$d]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCBS/${CCBS[$e]}/g" $OUTT> $OUTF`
 															        `cp $OUTF $OUTT`
																`sed "s/CCPLAYT/${CCPLAYT[$ff]}/g" $OUTT> $OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCPULLR/${CCPULLR[$af]}/g" $OUTT> $OUTF`
                                                                                                                                `cp $OUTF $OUTT`
																`sed "s/CCTS/${CCTS[$f]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCALPHAUP/${CCALPHAUP[$g]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCALPHADW/${CCALPHADW[$h]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCPIUP/${CCPIUP[$i]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCPIDW/${CCPIDW[$l]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCOMEGAP1/${CCOMEGAP1[$m]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCOMEGAP2/${CCOMEGAP2[$n]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCPPSEL/${CCPPSEL[$o]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCDDELAY/${CCDDELAY[$p]}/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCRHOP1/$CCRHOP1/g" $OUTT>$OUTF`
																`cp $OUTF $OUTT`
																`sed "s/CCRHOP2/$CCRHOP2/g" $OUTT>$OUTF`														
																if [[ ${CCDDELAY[$p]} > 0 ]]
																	then															
																	`cp $OUTF $OUTT`
																	`sed "s/CCMUDELAY/$CCMUDELAY/g" $OUTT>$OUTF`
																	`cp $OUTF $OUTT`
																	`sed "s/CCDEVDELAY/$CCDEVDELAY/g" $OUTT>$OUTF`
																else
																	`cp $OUTF $OUTT`
																	`sed "s/CCMUDELAY/0/g" $OUTT>$OUTF`
																	`cp $OUTF $OUTT`
																	`sed "s/CCDEVDELAY/0/g" $OUTT>$OUTF`															
																fi														
																rm $OUTT;
															fi
														done													
													done
												#done
											done
										#done
									#done
								#done
								done
								done
							done
						done
					done
				done
			done
		done
	done
done
