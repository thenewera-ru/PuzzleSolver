Below is the complete description of the algorithm _A star_ or _Smart Dijkstra_ to solve
"N-puzzle" problem. See [link](https://en.wikipedia.org/wiki/15_puzzle) for more details.
![Introduction](./images/Introduction.png)
![BoardStructure](./images/BoardStructure.png)
A* tries to approximate each state towards final configuration via so called heuristic function (in our example Manhattan distance and Euclidean).
![StateBeginning](./images/Score_p_1.png)
*Error for a fixed cell*

![TotalError](./images/TotalError.png)
*Total error for a state(board)*