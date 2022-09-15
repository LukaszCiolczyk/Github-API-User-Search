package com.company;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GUI {
    JFrame frame;
    JPanel panel;
    JLabel queryLabelText;
    JButton runQueryButton;
    JTextArea outputText;


    public GUI(int _width, int _height){
        //Creating the UI
        panel = new JPanel();
        panel.setLayout(null);
        panel.setSize(_width,_height);


        frame = new JFrame();
        frame.setSize(_width,_height);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Zadanie Atipera");
        frame.setVisible(true);
        frame.add(panel);


        queryLabelText = new JLabel("User name");
        queryLabelText.setBounds(200,0,100,25);
        panel.add(queryLabelText);


        TextField searchTextField = new TextField(20);
        searchTextField.setBounds(120,30,165,25);
        panel.add(searchTextField);


        runQueryButton = new JButton("Search");
        runQueryButton.setBounds(285,30,95,25);
        runQueryButton.addActionListener(e -> getDataAboutUser(searchTextField.getText()));
        panel.add(runQueryButton);


        outputText = new JTextArea("");
        JScrollPane scrollableOutputTextArea = new JScrollPane(outputText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableOutputTextArea.setBounds(20,100,500,400);
        panel.add(scrollableOutputTextArea);
    }


    private void getDataAboutUser(String _query){
        outputText.setText("");
        try{
            // Sending an HTTP GET request
            HttpResponse<String> getResponse = sendHttpGet("https://api.github.com/search/repositories?q=user:"+""+_query.replace(" ", "_"));

            // Status/error check
            if(getResponse.statusCode() !=200){
                outputText.setText("{\n" +
                        "\n" +
                        "        “status”: ${"+getResponse.statusCode()+"}\n" +
                        "\n" +
                        "        “Message”: ${"+"While looking for the user with name \" "+_query+"\" "+new JSONObject(getResponse.body()).get("message")+"}\n" +
                        "\n" +
                        "}");
                throw new Exception(outputText.getText());
            }else{

                JSONObject myResponse = new JSONObject(getResponse.body());
                createOutput(myResponse);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void createOutput(JSONObject _obj){
        JSONObject _output = new JSONObject();
        JSONArray _temp = _obj.getJSONArray("items");

        //Creating a result and organizing it
        for (Object o : _temp) {
            JSONObject jsonObject = (JSONObject) o;
            if (!((boolean) jsonObject.get("fork"))) {
                JSONObject _outputO = new JSONObject();
                JSONObject owner = jsonObject.getJSONObject("owner");
                _outputO.append("Repository_Name", jsonObject.get("name"));
                _outputO.append("Owner_Login", owner.get("login"));

                JSONArray _outputA = new JSONArray();
                _outputA.put(_outputO);
                JSONObject check = findTheBranches("https://api.github.com/repos/" + owner.get("login") + "/" + jsonObject.get("name") + "/branches");
                if (check != null)
                    _outputA.put(check);
                _output.append("Repositories", _outputA.toString().substring(1, _outputA.toString().lastIndexOf("]") + 1));
            }
        }
        //Getting rid of all unnecessary elements and displaying the answers.
        outputText.setText( outputText.getText()+"\n"+ _output.toString(_temp.length()).replace("\\",""));
    }


    JSONObject findTheBranches(String branches_url){
        try {
            // Sending an HTTP GET request
            HttpResponse<String> getResponse = sendHttpGet(branches_url);

            // Status/error check
            if(getResponse.statusCode() !=200){
                outputText.setText("{\n" +
                        "\n" +
                        "        “status”: ${"+getResponse.statusCode()+"},\n" +
                        "\n" +
                        "        “Message”: ${"+"While looking for branches: "+new JSONObject(getResponse.body()).get("message")+"}\n" +
                        "\n" +
                        "}");
                return null;
            }else{

                //Extracting information about branches
                JSONArray preresponse = new JSONArray(getResponse.body());
                JSONArray response = new JSONArray();
                for (Object o : preresponse) {
                    JSONObject jsonObject = (JSONObject) o;
                    JSONObject _response = new JSONObject();

                    _response.put("Branch_Name", jsonObject.get("name"));
                    JSONObject _temp = jsonObject.getJSONObject("commit");
                    _response.put("sha", _temp.get("sha"));
                    response.put(_response);
                }
                JSONObject branches = new JSONObject();
                branches.append("Branches",response.toString().substring(1,response.toString().lastIndexOf("]")+1));
                return branches;
            }
        }catch (Exception e){
            e.printStackTrace();
            return new JSONObject(e.getMessage());
        }
    }


    static HttpResponse<String> sendHttpGet(String _uri) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(_uri))
                .GET()
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        return httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }

}
