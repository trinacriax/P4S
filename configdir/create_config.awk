BEGIN{
file="config-p4s-template.txt"
}
{
if($0 ~ "PEERS"){
	printf "Peersize "
	for(i=2 ;i<=NF;i++){
		peer[i-1]=$i
		printf "%d; ",peer[i-1]
		}
		printf "\n"
	}
else if($0 ~ "CHUNKS"){
	printf "Chunks "
	for(i=2 ;i<=NF;i++){
		chunks[i-1]=$i
		printf "%d; ",chunks[i-1]
		}
		printf "\n"
	}	
else if($0 ~ "RTTDELAY"){	
	printf "Delays %d ",NF
	d=1;
	for(i=2 ;i<=NF;i++){
		dmin[d]=$i		
		printf "%d-",dmin[d]
		i++;
		dmax[d]=$i
		printf "%d; ",dmax[d]
		d++;		
		}
		printf "\n"
	}
else if($0 ~ "STREAMING"){
	printf "BS "
	for(i=2 ;i<=NF;i++){
		bs[i-1]=$i
		printf "%d; ",bs[i-1]
		}
		printf "\n"
	}
else if($0 ~ "PLAYTIME"){
	printf "PLAYT "
	for(i=2 ;i<=NF;i++){
		play[i-1]=$i
		printf "%d; ",play[i-1]
		}
		printf "\n"
	}
else if($0 ~ "PULLROUND"){
	printf "PULLRND "
	for(i=2 ;i<=NF;i++){
		pullrnd[i-1]=$i
		printf "%d; ",pullrnd[i-1]
		}
		printf "\n"
	}
else if($0 ~ "NEWCHUNK"){
	printf "New Chunk "
	for(i=2 ;i<=NF;i++){
		ts[i-1]=$i
		printf "%d; ",ts[i-1]
		}
		printf "\n"
	}
else if($0 ~ "BMP"){
	printf "Bmp "
	for(i=2 ;i<=NF;i++){
		bmp[i-1]=$i
		printf "%d; ",bmp[i-1]
		}
		printf "\n"
	}	
else if($0 ~ "CONNECT"){
	printf "Connect "
	d=1;
	for(i=2 ;i<=NF;i++){
		au[d]=$i
		printf "au[%d]=%d,",d,au[d]
		i++;
		ad[d]=$i
		printf "%d,",ad[d]
		i++
		pu[d]=$i		
		printf "%d,",pu[d]
		i++;
		pd[d]=$i
		printf "%d; ",pd[d]		
		d++;
		}	
		printf "\n"
	}
else if($0 ~ "WINDOW"){
	printf "Windows "
	d=1;
	for(i=2 ;i<=NF;i++){
		pushw[d]=$i
		printf "%d,",pushw[d]
		i++;
		pullw[d]=$i
		printf "%d; ",pullw[d]		
		d++;
		}	
		printf "\n"
	}
else if($0 ~ "DELAY"){
	printf "Delay "
	for(i=2 ;i<=NF;i++){
		delty[i-1]=$i
		printf "%d; ",delty[i-1]
		}
		printf "\n"
	}
else if($0 ~ "SELECT"){
	printf "Select "
	for(i=2 ;i<=NF;i++){
		sel[i-1]=$i
		printf "%d; ",sel[i-1]
		}
		printf "\n"
	}
else if($0 ~ "DEGREE"){
	printf "Degree "
	for(i=2 ;i<=NF;i++){
		deg[i-1]=$i
		printf "%d; ",deg[i-1]
		}
		printf "\n"
	}
else if($0 ~ "PUSHRET"){
	printf "Push attempts "
	for(i=2 ;i<=NF;i++){
		pushat[i-1]=$i
		printf "%d; ",pushat[i-1]
		}
		printf "\n"
	}
else if($0 ~ "PULLRET"){
	printf "Pull attempts "
	for(i=2 ;i<=NF;i++){
		pullat[i-1]=$i
		printf "%d; ",pullat[i-1]
		}
		printf "\n"
	}
}
END{
	configz=10400;	
	srand(systime())
	print configz
	for(a=1; a<= length(peer); a++){
		for(b=1; b<= length(chunks); b++){
			for(c=1; c<= length(dmin); c++){
				for(d=1; d<= length(bs); d++){
					for(e=1; e<= length(play); e++){
						for(f=1; f<= length(pullrnd); f++){
							for(g=1; g<= length(ts); g++){
								for(h=1; h<= length(bmp);h++){
									for(i=1; i<= length(au); i++){
										for(l=1; l<= length(pushw); l++){
											for(m=1; m<= length(delty); m++){
												for(n=1; n<= length(sel); n++){
													for(o=1; o<= length(deg); o++){
														for(p=1; p<= length(pushat); p++){
															for(r=1; r<= length(pullat); r++){
																if(ts[g]>dmax[c]){
																	configz++;
																	seed=sprintf("%d",(11118007199*rand()))
																	outo=sprintf("config-p4s-template.txt"  , configz)																		
																	outf=sprintf("config-p4s-0%d.txt"  , configz)
																	cmd=sprintf("cp %s %s",outo,outf)
																	system(cmd)
																	outt= sprintf("config-p4s-0%d.tmp"  , configz)
																	system(sprintf("cp %s %s",outf,outt))
																	printf ("Building configuration, seed %d; Config file is %s\n",seed, outf);
																	system(sprintf ("cp %s %s",outf,outt))
																#echo  "Net Size "${CCPEERS[$a]}" Chunk Size "${CCCHUNKS[$b]}" DEGREE "${CCDEGREE[$c]}" Bs "${CCBS[$e]}" Ts "${CCTS[$f]}" Delay Range ["${CCDMIN[$d]}":"${CCDMAX[$d]}"] AlphaUp "${CCALPHAUP[$g]}", AlphaDw "${CCALPHADW[$h]}", PiUp "${CCPIUP[$i]}", PiDw "${CCPIDW[$l]}" Omega_Push "${CCOMEGAP1[$m]}", Omega_Pull "${CCOMEGAP2[$n]}", Delay "${CCDDELAY[$p]}" Sel "${CCPPSEL[$o]}" Seed is $CCSEED" 
																system(sprintf ("sed \"s/CCSEED/%d/g\" %s > %s", seed,outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPEERS/%d/g\" %s > %s", peer[a],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCCHUNKS/%d/g\" %s > %s", chunks[b],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCDMIN/%d/g\" %s > %s", dmin[c],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCDMAX/%d/g\" %s > %s", dmax[c],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCBS/%d/g\" %s > %s", bs[d],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPLAYT/%d/g\" %s > %s", play[e],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPULLR/%d/g\" %s > %s", pullrnd[f],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCTS/%d/g\" %s > %s", ts[g],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																banda=(1.0*ts[g])/(ts[g]-dmax[c]);
																#print "1Banda "banda
																banda=(((banda*10.0)+1)/10.0)+bmp[h];
																#print "2Banda "banda
																banda=sprintf("%.2f",banda);
																#print "3Banda "banda
																system(sprintf ("sed \"s/CCBMULT/%d/g\" %s > %s", banda,outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCALPHAUP/%d/g\" %s > %s", au[i],outt,outf))
																printf("a[%d]=%d",i,au[i]);
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCALPHADW/%d/g\" %s > %s", ad[i],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPIUP/%d/g\" %s > %s", pu[i],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPIDW/%d/g\" %s > %s", pd[i],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCOMEGAP1/%d/g\" %s > %s", pushw[l],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCOMEGAP2/%d/g\" %s > %s", pullw[l],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCDDELAY/%d/g\" %s > %s", delty[m],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCPPSEL/%d/g\" %s > %s", sel[n],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCDEGREE/%d/g\" %s > %s", deg[o],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCRHOP1/%d/g\" %s > %s", pushat[p],outt,outf))
																system(sprintf ("cp %s %s",outf,outt))
																system(sprintf ("sed \"s/CCRHOP2/%d/g\" %s > %s", pullat[r],outt,outf))
																system(sprintf ("rm -f %s",outt))
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
