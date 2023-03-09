import requests

__API__ = "http://api.conceptnet.io"
__LANG__ = "en"

def cat(*args):
	return "/".join([a for a in args])

def request(head, *details):
	return requests.get(cat(__API__, head, __LANG__, *details)).json()

class Entity:
	def __init__(self, head):
		self.__HEAD__ = head

	def request(self, *details):
		return request(self.__HEAD__, *details)

class Concept(Entity):
	def __init__(self, label):
		Entity.__init__(self, "c")
		self.label = label

		# Be lazy; we don't query the API until necessary. When we do, set self.active to True.
		self.active = False

		# Cache of all "relate" relations from this to another concept
		self.relate_cache = {}

	def __str__(self):
		return "Concept({})".format(self.label)

	def _activate(self):
		if not self.active:
			self.data = self.request(self.label)
			# list of all edges, each of which is a dict
			self.edges = self.data["edges"]
			self.active = True

	def related(self, relation):
		self._activate()

		if relation not in Relation.__RELS__:
			raise RuntimeError("Invalid relation type `{}`. Relation should be one of: {}".format(relation, Relation.__RELS__))

		if relation in self.relate_cache:
			return self.relate_cache[relation]
		else:
			results = [edge for edge in self.edges if edge["rel"]["label"] == relation]
			new_concepts = self.new_concepts(results)
			self.relate_cache[relation] = new_concepts
			return new_concepts

	def new_concepts(self, list_of_edges):
		self._activate()

		concepts = []
		for e in list_of_edges:
			if e["end"]["label"] == self.label and e["start"]["language"] == __LANG__:
				concepts.append(Concept(e["start"]["label"]))
			elif e["start"]["label"] == self.label and e["end"]["language"] == __LANG__:
				concepts.append(Concept(e["end"]["label"]))
		return concepts


class Relation(Entity):
	__RELS__ = {"IsA", "RelatedTo", "PartOf", "HasA", "Causes", "HasSubevent", "HasPrerequisite", \
		"MotivatedByGoal", "Synonym", "DefinedAs", "Entails", "MannerOf"}
