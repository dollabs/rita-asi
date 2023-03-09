package jakeBarnwell.concept;

enum EntityOperator {
	MERGE_SEQ(0.2),
	EXPAND_SEQ(0.2),
	CHANGE_SPOOL(0.99),
	NOT(1),
	SWAP_SRO(1),
	SWAP_SGO(1),
	CHANGE_ELE(1),
	ADD_ELE(1),
	REMOVE_ELE(1);
	
	private final double cost;
	
	private EntityOperator(double c) {
		this.cost = c;
	}
	
	public double getCost() {
		return this.cost;
	}
}