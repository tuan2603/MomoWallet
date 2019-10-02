# MomoWallet

add cordova plugin

ionic cordova plugin add https://github.com/tuan2603/MomoWallet.git --save

// declare the cordova variable at the top of your class, right after your imports 
declare var cordova;

// check device platform before call the requestPayment function  to get token momo
if (this.platform.is('cordova')) {

const momo = cordova.plugins.MomoWallet;


momo.requestPayment((val) => {
          console.log(val);
        },
        (err) => {
            console.log(err);
        });

}
