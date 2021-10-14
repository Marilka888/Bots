import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;

public class Main {
    public static void main(String[] args) throws ClientException, ApiException, InterruptedException {

        TransportClient transportClient = new HttpTransportClient();//через него передаём запросы
        VkApiClient vk = new VkApiClient(transportClient);//интерфейс взаимодействия с Vk API
        Random random = new Random();

        Keyboard keyboard = new Keyboard();//add keyboard

        List<List<KeyboardButton>> allKey = new ArrayList<>();
        List<KeyboardButton> line1 = new ArrayList<>();//ряд, куда добавляем кнопки

        //добавляем варианты
        line1.add(new KeyboardButton().setAction(new KeyboardButtonAction().setLabel("Привет")
                .setType(KeyboardButtonActionType.TEXT)).setColor(KeyboardButtonColor.POSITIVE));
        line1.add(new KeyboardButton().setAction(new KeyboardButtonAction().setLabel("Что умеешь?")
                .setType(KeyboardButtonActionType.TEXT)).setColor(KeyboardButtonColor.POSITIVE));

        allKey.add(line1);//добавляем линию с кнопками
        keyboard.setButtons(allKey);


        GroupActor actor = new GroupActor(169265593, "YOUR_TOKEN" );
        Integer ts = vk.messages().getLongPollServer(actor).execute().getTs(); //идентификатор сообщения, с которого начинается обработка сообщений (обновляем)

        while (true){
            MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(actor).ts(ts);//история запросов
            List<Message> messages = historyQuery.execute().getMessages().getItems();//список сообщений
            if (!messages.isEmpty()){
                messages.forEach(message -> {//read messages
                    System.out.println(message.toString());//вывод входящих сообщений
                    try {
                        if (message.getText().equals("Привет")){ //получаем сообщение от user
                            vk.messages().send(actor).message("Привет, Зилибобка:)").userId(message.getFromId()).
                                    randomId(random.nextInt(10000)).execute();//отвечаем на сообщение user
                        }
                        else if (message.getText().equals("Что умеешь?")){
                            vk.messages().send(actor).message("Я многозадачная. Умею плакать и работать одновременно)").userId(message.getFromId()).
                                    randomId(random.nextInt(10000)).execute();
                        }
                        else if (message.getText().equals("КНОПКИ ДАЙ")) {
                            vk.messages().send(actor).message("Ну на").userId(message.getFromId()).
                                    randomId(random.nextInt(10000)).keyboard(keyboard).execute();
                        }
                        else { //непредусмотренный случай
                            vk.messages().send(actor).message("Что ты высрал?))").userId(message.getFromId()).
                                    randomId(random.nextInt(10000)).execute();
                        }

                    }
                    catch (ApiException | ClientException e) {e.printStackTrace();}
                });
            }
            ts = vk.messages().getLongPollServer(actor).execute().getTs();//обновление
            Thread.sleep(500);
        }
    }
}
