/** The same graph files the desktop app ships, inline so tests need no I/O. */
export const SAMPLES: Record<string, string> = {
  'bowtie.txt': `0 1
0 2
1 2
2 3
2 4
3 4`,
  'cluster.txt': `0 1
0 2
0 3
1 2
1 3
2 3
3 4
4 5
4 6
5 6`,
  'sample-small.txt': `0 1
0 2
1 2
1 3
2 3
3 4
4 5
4 6
5 6
6 7`,
};
