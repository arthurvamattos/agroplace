package br.edu.ifro.agroplace.helper;

public class WhatsAppHelper {
    private static String baseAPI = "https://api.whatsapp.com/send?phone=55";

    public static String gerarLinkAPI(String telefone){
        return baseAPI + telefone;
    }
}
