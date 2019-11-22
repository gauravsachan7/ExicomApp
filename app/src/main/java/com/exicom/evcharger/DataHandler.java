package com.exicom.evcharger;

import android.support.v7.app.AppCompatActivity;

public class DataHandler extends AppCompatActivity {
        public String id;
        public String charger_serial_no;
        public String nick_name;
        public String flag;
        public String client_certificate;
        public String created_by;

        public String getId(){
            return  id;
        }
        public void setId(String id){
            this.id = id;
        }
        public String getCharger_serial_no(){
            return  charger_serial_no;
        }
        public void setCharger_serial_no(String name){
            this.charger_serial_no = name;
        }
        public String getMac_add(){
            return  nick_name;
        }
        public void setMac_add(String nick_name){
            this.nick_name = nick_name;
        }
        public String getFlag(){
            return  flag;
        }
        public void setFlag(String email){
            this.flag = email;
        }
        public void setNick_name(String nick_name){
            this.nick_name = nick_name;
        }
        public String getNickName(){
            return nick_name;
        }
        public void setClient_certificate(String client_certificate){
            this.client_certificate = client_certificate.substring(6);
        }
        public String getClient_certificate(){
            return client_certificate;
        }
        public void setCreated_by(String created_by){
            this.created_by = created_by;
        }
        public String getCreated_by(){
            return created_by;
        }
    }


