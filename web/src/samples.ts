/** Graphs the demo offers, chosen to be small enough to read at a glance. */
export interface Sample {
  readonly name: string;
  readonly text: string;
}

export const SAMPLES: Sample[] = [
  {
    name: 'Bowtie',
    text: '0 1\n0 2\n1 2\n2 3\n2 4\n3 4',
  },
  {
    name: 'Kite',
    text: '0 1\n0 2\n1 2\n1 3\n2 3\n3 4',
  },
  {
    name: 'Cluster',
    text: '0 1\n0 2\n0 3\n1 2\n1 3\n2 3\n3 4\n4 5\n4 6\n5 6',
  },
  {
    name: 'Chain',
    text: '0 1\n1 2\n2 3\n3 4\n4 5\n0 2\n3 5',
  },
];
