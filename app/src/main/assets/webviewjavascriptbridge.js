;
!(function () {
  "use strict";
  if (window.bridge) {
    return;
  }

  var current = 0;
  var callbacks = { };
  var callbackNotDelete = [];

  var sendQueue = [];
  var receiveQueue = [];
  var handlers = [];

  var nativeComponents = {
    get: function get(componentType, callback) {
        _send({ data: { 'a': 'getComponents', 'd': JSON.parse(componentType)} }, callback);
    },
    getAll: function getAll(callback) {
        _send({ data: { 'a': 'getAllComponents' } }, callback);
    }
  };

  function sendVideo(id, message, callback) {
     message = JSON.parse(message);
     message.componentId = id;
     _sendVideo({ data: { 'a': 'videoSendAction', 'd': message} }, callback);
  }

  function onVideo(id, message, callback) {
     _onVideo({ data: { 'a': 'videoOnAction', 'd': message} }, callback);
  }

  function _sendVideo(message, callback) {
    if (callback) {
      var cid = '' + (++current);
      callbacks[cid] = callback;
      message['cid'] = cid;
    }
    video.send(JSON.stringify(message));
  }

  function _onVideo(message, callback) {
    if (callback) {
      var cid = '' + (++current);
      callbacks[cid] = callback;
      callbackNotDelete.push(cid);
      message['cid'] = cid;
    }
    video.on(JSON.stringify(message));
  }

  function init(handler) {
    if (window._handler) {
      throw new Error('init called twice');
    }
    bridge._handler = handler;
    var receive = receiveQueue;
    receiveQueue = null;
    for (var i in receive) {
      _dispatch(receive[i]);
    }
  }

  function saveRule(data, callback) {
    _sendRule({ data: { 'a': 'saveRule', 'd': JSON.parse(data)} }, callback);
  }

  function getUnitsSpec(callback) {
  	_send({ data: { 'a':'getUnitsSpec' } }, callback)
  }

    function getContacts(callback) {
      _send({ data: { 'a': 'getContacts' } }, callback);
    }

  function _sendRule(message, callback){
    if(callback) {

    }
    android.call(JSON.stringify(message));
  }

  function _send(message, callback) {
    if (callback) {
      var cid = '' + (++current);
      callbacks[cid] = callback;
      message['cid'] = cid;
    }
    android.call(JSON.stringify(message));
  }

  function openUrl(url,callback){
    console.log("url: "+url);
    _send({ data: { 'a': 'openUrl', 'd': { 'value': url }} }, callback);
  }

  function _sendOp(op) {
    return function(data,callback) {
      _send({ data: { 'a': op, 'd': JSON.parse(data)} }, callback);
    }
  }

  function _dispatch(message) {
    var json = JSON.parse(message);
    if (json.rcid) {
      var callback = callbacks[json.rcid];
      if (callback) {
        if(callbackNotDelete.indexOf(json.rcid) === -1) {
          delete callbacks[json.rcid];
        }
        var data = json.data;
        callback(data);
      }
    } else {

      var handler = bridge._handler;

      try {
        handler(json.data, null);
      } catch (exception) {
        if (typeof console != 'undefined') {
          console.log("WVJSBridge [WARNING] js handler threw " + exception.stack);
        }
      }
    }
  }

  function jsonEscape(str)  {
      return str.replace(/\n/g, "\\\\n").replace(/\r/g, "\\\\r").replace(/\t/g, "\\\\t");
  }

  function _handle(msg) {
    var message = jsonEscape(msg);
    console.log("handling message: "+message);
    if (receiveQueue) {
      receiveQueue.push(message);
    } else {
      _dispatch(message);
    }
  }

  window.bridge = {
    nativeComponents: nativeComponents,
    init: init,
    subscribe: _sendOp('subscribe'),
    unsubscribe: _sendOp('unsubscribe'),
    publish: _sendOp('publish'),
    saveRule: saveRule,
    getUnitsSpec: getUnitsSpec,
    getContacts: getContacts,
    openUrl: openUrl,
    _handle: _handle
  };

  window.bridgeVideo = {
    send: sendVideo,
    on: onVideo
  };

  var readyEvent = document.createEvent('Events');
  readyEvent.initEvent('WebViewJavascriptBridgeReady');
  readyEvent.bridge = bridge;
  readyEvent.bridgeVideo = bridgeVideo;
  readyEvent.options = android.options() || '{}';
  document.dispatchEvent(readyEvent);
  android.ready();
})();
