import conceptnet as cnet

def pretty_print(iterable):
	return map(str, iterable)

def search(start_label, end_label):
	""" Does a BFS starting with the concept of start_label 
	and ending at the concept at end_label. Returns an error 
	if it takes too long or the end concept is never found."""

	visited = set()

	def get_nexts(concept):
		""" Iterates through all types of relations to get relateds, only 
		ones that have never been seen before. """
		nexts = []
		for rel in cnet.Relation.__RELS__:
			nexts.extend(concept.related(rel))
		nexts = set(nexts) - visited
		visited.update(nexts)

		return nexts

	start_c = cnet.Concept(start_label)
	end_c = cnet.Concept(end_label)

	agenda = [[start_c]]
	iters = 0
	while len(agenda) > 0:
		if iters > 1000:
			raise Error("Took too long!")

		current_path = agenda.pop(0)

		print pretty_print(current_path)

		# Check if done 
		if current_path[-1].label == end_label:
			return current_path 

		# List of next possible concepts in network (i.e. connected)
		nexts = get_nexts(current_path[-1])
		# Extend by adding them all to the end
		extensions = [current_path + [next] for next in nexts]


		# Add extensions to end of agenda
		agenda.extend(extensions)

		iters += 1

search("slap", "hurt")

# co = Concept("harm")
# co.relate("IsA")