public class QueryDB {
    public void main(String[] args) {
        int choice = Integer.parseInt(args[0]);
        QueryDB queryDB = new QueryDB();
        if (choice == 1) {
            queryDB.Query1();
        } else if (choice == 2) {
            queryDB.Query2();
        } else if (choice == 3) {
            queryDB.Query3();
        } else if (choice == 4) {
            queryDB.Query4();
        } else if (choice == 5) {
            queryDB.Query5();
        } else {
            try {
                throw new Exception("Not a valid query option!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Query1() {

    }

    public void Query2() {

    }

    public void Query3() {

    }

    public void Query4() {

    }

    public void Query5() {

    }
}
