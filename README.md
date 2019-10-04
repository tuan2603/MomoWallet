# MomoWallet

//1. add cordova plugin

ionic cordova plugin add https://github.com/tuan2603/MomoWallet.git --save

//2. prepare android 

ionic cordova prepare android

//3 run app on android device

ionic cordova run android --address=0.0.0.0 -l

// if plgugin error remove plugin and do again step 1 - 3

ionic cordova plugin remove cordova-plugin-momo-wallet

// declare the cordova variable at the top of your class, right after your imports 

declare var cordova;

// check device platform before call the requestPayment function  to get token momo

//example
#home.page.html

<ion-content>
  <ion-button  (click)="requestPayment()" expand="block"> Thanh toan </ion-button>
</ion-content>

#home.page.ts

import { Component } from '@angular/core';
import { Platform } from '@ionic/angular';
declare var cordova;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  data = {
    amount: 0,
    merchantName : 'KPIS',
    merchantCode : 'MOMOIQA420180417',
    orderId: '012345999',
    orderLabel: 'Nap tien',
    merchantNameLabel: 'Service',
    total_fee: 0,
    description: 'nap tien vao vi' ,
    appScheme: 'TM-Shein',
    objExtraData: JSON.stringify({
      site_code: '008',
      site_name: 'CGV Cresent Mall',
      screen_code: 0,
      screen_name: 'Special',
      movie_name: 'Kẻ Trộm Mặt Trăng 3',
      movie_format: '2D',
    }),

  };

  constructor(private platform: Platform) {

  }

  requestPayment() {
     this.data.amount = 10;
     this.platform.ready().then(() => {
      if (this.platform.is('cordova')) {
        const momo = cordova.plugins.MomoWallet;
        console.log(this.platform);
        momo.requestPayment(
          this.data, ( data ) => {
            alert(data);
            console.log('data', data);
          }, (error) => {
            alert(error);
            console.log('error', error);
          });
      }
    });
 }

}
