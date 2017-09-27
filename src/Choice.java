/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */
class Choice {

    String value;
    boolean empty;

    public Choice() {
        value = "";
        empty = true;
    }

    public void reset() {
        value = "";
        empty = true;
    }

    public void setValue(String value) {
        this.value = value;
        empty = false;
    }
}
