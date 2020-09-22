class Main{
   public static void main(String[] args){
        try {
            Client client = new Client("224.0.0.1", 8123);
            client.goCast();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

