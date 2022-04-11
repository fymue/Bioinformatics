import numpy as np

#Python implementation of the viterbi algorithm
#calculates the most likely (hidden) state path
#based on a hidden markov model (HMM)

def viterbi(states, emissions, transition_p, emission_p, observed_emissions, initial_p=None) -> str:
    best_state_path = []
    transition_p = np.array(transition_p)
    emission_p = np.array(emission_p)
    total_obs = len(observed_emissions)
    total_states = len(states)

    #if the initital probabilities of each state have not been set,
    #assume that the entry to every state has the same probability
    if not initial_p: initial_p = [1 / total_states] * total_states

    #matrix to store the best path probabilities
    viterbi_matrix = np.zeros(shape=(total_states, total_obs), dtype=np.float32) 

    #matrix to store the indices of the states associated with the current best path probability (used in backtracing later)
    state_matrix = np.zeros(shape=(total_states, total_obs), dtype=np.int16) 

    #initialize the first column of the matrix (==start of the state path)
    for s in range(len(states)): viterbi_matrix[s, 0] = initial_p[s] * emission_p[s][emissions[observed_emissions[0]]]

    #calculate the probability of each state path (refer to the recursive definition of the algorithm for details)
    for o in range(1, total_obs):
        for s in range(len(states)):
            k = np.argmax(viterbi_matrix[:,o-1] * transition_p[:,s] * emission_p[s,])
            viterbi_matrix[s, o] = viterbi_matrix[k, o-1] * transition_p[k, s] * emission_p[s, emissions[observed_emissions[o]]]
            state_matrix[s, o] = k

    #find the start state for the backtracing (will be the last state of the best state path at the end)
    k = np.argmax(viterbi_matrix[:,total_obs-1])

    #find the best state path using the state/pointer matrix built earlier (backtracing)
    for o in range(total_obs-1, -1, -1):
        best_state_path.append(states[k])
        k = state_matrix[k, o]

    #reverse the best state path since we started at the end
    best_state_path.reverse()

    return " ".join(best_state_path)

def main():
    #example inputs:

    #states: array-like; states of the HMM as strings
    states = ("Hoch", "Tief")

    #emissions: dict; emissions of the HMM as strings, indices of the emissions in the emission probability matrix (see below)
    emissions = {"Sonne" : 0, "Regen" : 1}

    #transition_p: nested array-like (matrix); transition probalilites between states
    #(order has be the same as order in states!)
    transition_p = [[0.7, 0.3], [0.4, 0.6]]

    #emission_p: nested array-like (matrix); emission probalilites at every state
    #(array order has be the same as order in states, emissions order/indices have to correspond to values in emissions!)
    emission_p = [[0.8, 0.2], [0.1, 0.9]]

    #initial_p: intitial probabilities of all states (can be provided; if not, every state gets the same initial probability)
    initial_p = [0.5, 0.5]

    #observed_emissions: array-like, observed emissions to use for the calculation of the most likely state path
    observed_emissions = ("Sonne", "Sonne", "Regen", "Regen", "Sonne", "Regen", "Sonne", "Sonne", "Regen")

    print(viterbi(states, emissions, transition_p, emission_p, observed_emissions))

if __name__ == "__main__": main()

