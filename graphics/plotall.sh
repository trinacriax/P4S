filename=`ls -1 ../cdf_avg_delay*.dat`
if [ -f "$filename" ]
then	
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename  plotCDFAvgDelay  $out
	mv plotCDFAvgDelay.gnu ..
fi
filename=`ls -1 ../cdf_diffusion_chunks*.dat`
if [ -f "$filename" ]
then	
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotCDFChunksDiffusion  $out
	mv plotCDFChunksDiffusion.gnu ..	
fi
filename=`ls -1 ../cdf_diffusion_peers*.dat`
if [ -f "$filename" ]
then
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotCDFPeersDiffusion  $out
	mv plotCDFPeersDiffusion.gnu ..	
fi
filename=`ls -1 ../delays_on_chunks*.dat`
if [ -f "$filename" ]
then
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotDelaysChunks $out
	mv plotDelaysChunks.gnu ..	
fi
filename=`ls -1 ../delays_on_peers*.dat`
if [ -f "$filename" ]
then
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotDelaysPeers $out
	mv plotDelaysPeers.gnu ..	
fi
filename=`ls -1 ../delays_histogram*.dat`
if [ -f "$filename" ]
then
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotDelaysHistogram $out
	mv plotDelaysHistogram.gnu ..	
fi
filename=`ls -1 ../op_distr*.dat`
if [ -f "$filename" ]
then
	out=`echo "$filename"|sed -e 's/..\///'|sed -e 's/.dat//'`
	echo "Out>"$out"<"
	bash create_gnu.sh $filename plotOpDistribution $out
	mv plotOpDistribution.gnu ..	
fi


cd ..
for i in `ls *.gnu`;
do 
	gnuplot $i;
done
