// xender: 国际版, shanchuan: 国内版, onetouch: alcatel版
var isInter="xender" ;//  = 'xender';
try{
	define([], function(){

		var onOff = {
		};
		onOff.init = (function(){
			
			return{
				getIsInter: function(){
					return isInter;
				}
			}
		})();
		
		return onOff;
	});
}catch(e){
	
}

