package eu.novi.ponder2.policy;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.policy.Pair;

/**
 * @author gio Pair is a support class that is used to represents the pairs of
 *         OIDs. These pairs represent all the possible combinations of OIDs
 *         that are part of the subject and target path. firstElement contains
 *         the subject OID or of any other domains that are in the subject path.
 *         secondElement contains the target OID or of any other domains that
 *         are in the target path.
 */
public class Pair {
	P2ManagedObject firstElement, secondElement;
	int subjectDist, targetDist;

	public Pair(P2ManagedObject firstElement, P2ManagedObject secondElement) {
		super();
		// TODO Auto-generated constructor stub
		this.firstElement = firstElement;
		this.secondElement = secondElement;
		subjectDist = -1;
		targetDist = -1;
	}

	public Pair(P2ManagedObject firstElement, P2ManagedObject secondElement,
			int subjectDist, int targetDist) {
		super();
		this.firstElement = firstElement;
		this.secondElement = secondElement;
		this.subjectDist = subjectDist;
		this.targetDist = targetDist;
	}

	public P2ManagedObject getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(P2ManagedObject firstElement) {
		this.firstElement = firstElement;
	}

	public P2ManagedObject getSecondElement() {
		return secondElement;
	}

	public void setSecondElement(P2ManagedObject secondElement) {
		this.secondElement = secondElement;
	}

	@Override
	public boolean equals(Object pair) {
		return ((this.firstElement.equals(((Pair) pair).firstElement)) && (this.secondElement
				.equals(((Pair) pair).secondElement)));
	}

	@Override
	public String toString() {
		return "{" + firstElement.getOID() + "," + subjectDist + ","
				+ secondElement.getOID() + "," + targetDist + "}";
	}

	int getSubjectDist() {
		return subjectDist;
	}

	void setSubjectDist(int subjectDist) {
		this.subjectDist = subjectDist;
	}

	int getTargetDist() {
		return targetDist;
	}

	void setTargetDist(int targetDist) {
		this.targetDist = targetDist;
	}
}