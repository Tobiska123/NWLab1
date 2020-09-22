class Main{
   public static void main(String[] args){
        try {
            Client client = new Client("224.0.0.0", 4446);
            client.goCast();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

