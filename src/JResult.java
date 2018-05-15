public class JResult {
    private String result = null;
    JResult() { }
    JResult(String result) {
        this.result = result;
    }
    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        if (result == null)
            return "";
        return getResult();
    }
}
