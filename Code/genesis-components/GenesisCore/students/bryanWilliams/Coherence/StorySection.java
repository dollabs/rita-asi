package bryanWilliams.Coherence;

public class StorySection {
    // both are INCLUSIVE
    private final int beginIndex;
    private final int endIndex;
    
    public StorySection(int begin, int end) {
        beginIndex = begin;
        endIndex = end;
    }
    
    public int getBeginIndex() {
        return beginIndex;
    }
    
    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + beginIndex;
        result = prime * result + endIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StorySection other = (StorySection) obj;
        if (beginIndex != other.beginIndex)
            return false;
        if (endIndex != other.endIndex)
            return false;
        return true;
    }
}
