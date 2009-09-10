set output "CCOUTFILE.eps"
set terminal postscript eps enhanced color font "Tahoma,14"
set ylabel "CDF Chunks"
set xlabel "Delay [sec]"
set mytics 5
set mxtics 4
set xtics nomirror
set key out horiz top center
set size 1,1
set style line 1 linetype 1 linewidth 3
set style line 2 linetype 2 linewidth 3
set style line 3 linetype 3 linewidth 3
set style line 4 linetype 4 linewidth 3
set style line 5 linetype 5 linewidth 3
set style line 6 linetype 7 linewidth 3
set style line 7 linetype 8 linewidth 3
set style line 8 linetype 9 linewidth 3
set style line 9 linetype 10 linewidth 3
set style line 10 linetype 11 linewidth 3
set style line 11 linetype 12 linewidth 3
set style line 12 linetype 13 linewidth 3
set style line 13 linetype 14 linewidth 3
set style line 14 linetype 15 linewidth 3
set style line 15 linetype 16 linewidth 3
set style line 16 linetype 17 linewidth 3
set style line 17 linetype 18 linewidth 3
set style line 18 linetype 19 linewidth 3
set style line 19 linetype 20 linewidth 3
set style line 20 linetype 21 linewidth 3
set style line 21 linetype 22 linewidth 3
set style line 22 linetype 23 linewidth 3
set style line 23 linetype 24 linewidth 3
set style line 24 linetype 25 linewidth 3
set style line 25 linetype 26 linewidth 3
set style line 26 linetype 27 linewidth 3
set style line 27 linetype 28 linewidth 3
set style line 28 linetype 29 linewidth 3
set style line 29 linetype 30 linewidth 3
set style line 30 linetype 31 linewidth 3
#set format y "10^{%L}"
set title "CDF Delay max vs upload bandwidth and peer selection"
plot \