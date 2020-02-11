Below is the complete description of the algorithm _A star_ or _Smart Dijkstra_ to solve
"N-puzzle" problem. See [link](https://en.wikipedia.org/wiki/15_puzzle) for more details.
![Introduction](./images/Introduction.jpg)
![BoardStructure](./images/BoardStructure.jpg)
A* tries to approximate each state towards final configuration via so called heuristic function (in our example Manhattan distance and Euclidean).
![StateBeginning](./images/Score_one.jpg)

![TotalError](./images/Score_two.jpg)
*Total error for a state(board)*