import{A as y,B as J,C as K,D,E as I,F as x,G as O,H as X,I as Z,J as m,K as N,L as E,M as ee,N as k,O as te,P as ne,Q as ie,R as h,S as oe,T as re,a as S,b as A,c as U,d as Q,e as w,f as B,g as f,h as b,i as V,j as q,k as W,l as v,m as _,n as F,o as c,p as M,r as P,s as $,t as z,u as l,v as a,w as u,x as p,y as G,z as Y}from"./chunk-PLY572PE.js";var j=class{constructor(){this.baseUrl="",this.user="",this.password=""}},d=(()=>{let n=class n{constructor(e,t){this.http=e,this.cfg=t,this.baseUrl="",this.baseUrl=this.cfg?.baseUrl||""}propfind(e,t){let i=`<?xml version="1.0" encoding="utf-8" ?>
 <D:propfind xmlns:D="DAV:">
  <D:prop>
`;return i+=`<D:creationdate/>
<D:displayname/>
<D:getcontentlength/>
<D:getcontenttype/>
<D:getetag/>
<D:getlastmodified/>
<D:resourcetype/>
`,i+=`  </D:prop>
 </D:propfind>`,this.sendRequest({method:"PROPFIND",uri:e,contentType:"text/xml",responseType:"text",headers:{Depth:t},body:i}).pipe(A(r=>{let s=r,T=new DOMParser().parseFromString(s.body,"text/xml").firstElementChild;if(T==null)return[];let L=[];return T.childNodes.forEach(C=>{if(!C.nodeName.endsWith("response"))return;let R=this.toObject(C);R.current=R.href.replaceAll(/\/$/g,"")==e.replaceAll(/\/$/g,""),L.push(R)}),L.map(C=>this.toFileInfo(C))}))}sendRequest(e){let t=new N;this.cfg?.password&&this.cfg?.user&&(t=t.append("Authorization","Basic "+btoa(this.cfg?.user+":"+this.cfg?.password))),e.contentType&&(t=t.append("Content-Type",e.contentType));for(let i in e.headers)t=t.append(i,e.headers[i]);return this.http.request(new E(e.method,(this.cfg?.baseUrl||"")+e.uri,e.body,{responseType:"text",headers:t})).pipe(U(i=>i.type!==0))}toFileInfo(e){return{href:e.href,status:e.propstat.status,contentLength:parseInt(e.propstat.prop.getcontentlength||"0"),mimeType:e.propstat.prop.getcontenttype||"",displayName:e.propstat.prop.displayname,etag:e.propstat.prop.getetag,isFolder:e.propstat.prop.resourcetype?.collection!=null,current:e.current||!1}}toObject(e){let t={};return e.childNodes.length<=1?e.textContent:(e.childNodes.forEach(i=>{if(i.nodeName.endsWith("#text"))return;let r=i.nodeName.split(":")[1];i.childNodes.length<=1?t[r]=i.textContent:t[r]=this.toObject(i)}),t)}move(e,t){}delete(e){}copy(e,t){}get(e){let t=new N;return this.cfg?.password&&this.cfg?.user&&(t=t.append("Authorization","Basic "+btoa(this.cfg?.user+":"+this.cfg?.password))),this.http.request(new E("GET",(this.cfg?.baseUrl||"")+"/"+e,null,{headers:t})).pipe(U(i=>i.type!==0))}};n.\u0275fac=function(t){return new(t||n)(B(ee),B(j,8))},n.\u0275prov=Q({token:n,factory:n.\u0275fac});let o=n;return o})();var H=(()=>{let n=class n{constructor(e){this.route=e}toHome(){this.current?.isFolder&&this.current?.href!="/"&&this.route.navigateByUrl("/")}toUp(){this.current?.isFolder&&this.current.href!="/"&&this.route.navigateByUrl(this.current.href.replaceAll(/\/[^/]*\/?$/g,""))}};n.\u0275fac=function(t){return new(t||n)(c(h))},n.\u0275cmp=f({type:n,selectors:[["left-panel"]],inputs:{current:"current"},standalone:!0,features:[D],decls:13,vars:2,consts:[[1,"go-up",3,"click"],["xmlns","http://www.w3.org/2000/svg"],["d","M12 4V14M12 4L8 8M12 4L16 8","stroke","#000000","stroke-width","2","stroke-linecap","round","stroke-linejoin","round"],[3,"click"]],template:function(t,i){t&1&&(l(0,"div",0),p("click",function(){return i.toUp()}),l(1,"b"),y(2,"\u041D\u0430 \u0432\u0435\u0440\u0445"),a(),q(),l(3,"svg",1),u(4,"path",2),a()(),W(),u(5,"br")(6,"br"),l(7,"b",3),p("click",function(){return i.toHome()}),y(8,"\u041D\u0430 \u0433\u043B\u0430\u0432\u043D\u0443\u044E"),a(),u(9,"br"),l(10,"b"),y(11,"\u041C\u0435\u0441\u0442\u0430"),a(),u(12,"br")),t&2&&P("active",(i.current==null?null:i.current.href)!="/")},dependencies:[m],styles:["[_nghost-%COMP%]{background:#F3F2F2;width:130px;min-width:100px;border-right:1px solid #C7C7C7}.arrow-up[_ngcontent-%COMP%]   path[_ngcontent-%COMP%]{fill:#651111}.go-up[_ngcontent-%COMP%]{-webkit-user-select:none;user-select:none;cursor:pointer;display:inline-flex;flex-direction:row;align-items:center;justify-content:center;stroke:#000}.go-up[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{height:18px}.go-up[_ngcontent-%COMP%]   svg[_ngcontent-%COMP%]{width:20px;height:20px}.go-up[_ngcontent-%COMP%]:not(.active){color:gray;cursor:default}.go-up[_ngcontent-%COMP%]:not(.active)   path[_ngcontent-%COMP%]{stroke:gray}"]});let o=n;return o})();var le=(()=>{let n=class n{constructor(e){this._viewContainer=e}ngOnInit(){}close(){if(this.cmpRef){this.cmpRef.destroy(),this.cmpRef=void 0;return}}click(e){return S(this,null,function*(){if(e.preventDefault(),this.cmpRef){this.cmpRef.destroy(),this.cmpRef=void 0;return}let{FilePopupComponent:t}=yield import("./chunk-F35XHBGW.js");this.cmpRef=this._viewContainer.createComponent(t),this.cmpRef.instance.event=e})}ngOnDestroy(){this.cmpRef&&(this.cmpRef.destroy(),this.cmpRef=void 0)}};n.\u0275fac=function(t){return new(t||n)(c($))},n.\u0275dir=V({type:n,selectors:[["","file-popup",""]],hostBindings:function(t,i){t&1&&p("click",function(){return i.close()},!1,v)("contextmenu",function(s){return i.close(s)},!1,v)("keydown.escape",function(s){return i.close(s)},!1,v)("contextmenu",function(s){return i.click(s)})},inputs:{fileInfo:"fileInfo",filePopup:["file-popup","filePopup"]}});let o=n;return o})();var Ce=["preview"],pe=(()=>{let n=class n{constructor(e){this.route=e,this.title="webdav"}ngOnInit(){let e=(this.baseUrl||"")+"/mimetype?icon-file&mimetype="+(this.fileInfo.isFolder?"folder":encodeURIComponent(this.fileInfo.mimeType));this.preview.nativeElement.src=e}dblclick(){if(this.fileInfo.isFolder){this.route.navigateByUrl(this.fileInfo.href);return}var e=this.fileInfo.href;window.open((this.baseUrl||"")+e,"Download")}getFileUrl(){return(this.baseUrl||"")+this.fileInfo.href}onFocus(){this.focus=!0}onFocusOut(){this.focus=!1}errorLoapPreview(){this.preview.nativeElement.src="favicon.ico"}};n.\u0275fac=function(t){return new(t||n)(c(h))},n.\u0275cmp=f({type:n,selectors:[["file-item"]],viewQuery:function(t,i){if(t&1&&x(Ce,7,_),t&2){let r;I(r=O())&&(i.preview=r.first)}},hostVars:2,hostBindings:function(t,i){t&1&&p("dblclick",function(){return i.dblclick()}),t&2&&P("focus",i.focus)},inputs:{fileInfo:"fileInfo",baseUrl:"baseUrl"},decls:5,vars:2,consts:[[3,"title","focus","focusout"],["src","icons",3,"error"],["preview",""]],template:function(t,i){t&1&&(l(0,"button",0),p("focus",function(){return i.onFocus()})("focusout",function(){return i.onFocusOut()}),l(1,"img",1,2),p("error",function(){return i.errorLoapPreview()}),a(),l(3,"span"),y(4),a()()),t&2&&(Y("title",i.fileInfo.displayName.length>20?i.fileInfo.displayName:""),F(4),J(i.fileInfo.displayName))},styles:["img[_ngcontent-%COMP%]{width:24px;height:24px;margin-right:10px}span[_ngcontent-%COMP%]{user-select:none;-webkit-user-select:none;-moz-user-select:none;-khtml-user-select:none;-ms-user-select:none;overflow:hidden}button[_ngcontent-%COMP%]{background:none;border:0;display:inline-flex;flex-direction:row;align-items:center;justify-content:start;width:100%;overflow:hidden}button[_ngcontent-%COMP%]:focus{outline:0}[_nghost-%COMP%]:hover   img[_ngcontent-%COMP%]{filter:brightness(1.15)}.compact[_nghost-%COMP%]   span[_ngcontent-%COMP%], .compact   [_nghost-%COMP%]   span[_ngcontent-%COMP%]{text-overflow:ellipsis;text-wrap:nowrap}.focus[_nghost-%COMP%]{background:#92B372}"]});let o=n;return o})();var be=["filesPanel"];function _e(o,n){if(o&1&&u(0,"file-item",4),o&2){let he=n.$implicit,e=G();M("fileInfo",he)("baseUrl",e.baseUrl)}}var ce=(()=>{let n=class n{constructor(e,t,i){this.route=e,this.webDav=t,this.router=i,this.title="webdav",this.baseUrl="",this.path="/",this.files=[],this.baseUrl=t.baseUrl,this.routeSubscription=this.router.events.subscribe(r=>{r instanceof ne&&(this.path=r.url,this.propfind())})}click(e){e.preventDefault()}propfind(){this.webDav.propfind(this.path,"1").subscribe(e=>{this.files=e.filter(t=>t.current?(this.current=t,!1):!0)})}get(){this.webDav.get("test/").subscribe()}ngOnDestroy(){this.routeSubscription?.unsubscribe()}onBackspace(e){this.toUp()}onMouseWheel(e){if(e.deltaY==0)return;let t=this.filesPanel.nativeElement;t.scrollTo(t.scrollLeft+e.deltaY,0)}toUp(){this.current.isFolder&&this.current.href!="/"&&this.route.navigateByUrl(this.current.href.replaceAll(/\/[^/]*\/?$/g,""))}};n.\u0275fac=function(t){return new(t||n)(c(h),c(d),c(h))},n.\u0275cmp=f({type:n,selectors:[["navigator"]],viewQuery:function(t,i){if(t&1&&x(be,7,_),t&2){let r;I(r=O())&&(i.filesPanel=r.first)}},hostBindings:function(t,i){t&1&&p("contextmenu",function(s){return i.click(s)})("keydown.backspace",function(s){return i.onBackspace(s)},!1,v)("wheel",function(s){return i.onMouseWheel(s)})},inputs:{current:"current"},decls:4,vars:2,consts:[[3,"current"],[1,"compact","files",2,"max-height","inherit"],["filesPanel",""],[3,"fileInfo","baseUrl","file-popup",4,"ngFor","ngForOf"],[3,"fileInfo","baseUrl","file-popup"]],template:function(t,i){t&1&&(u(0,"left-panel",0),l(1,"div",1,2),z(3,_e,1,2,"file-item",3),a()),t&2&&(M("current",i.current),F(3),M("ngForOf",i.files))},dependencies:[Z,H,le,pe],styles:["[_nghost-%COMP%]{display:flex;height:inherit}.compact.files[_ngcontent-%COMP%]{padding-top:5px;display:grid;width:100%;grid-template-columns:repeat(auto-fill,200px);grid-template-rows:repeat(auto-fill,35px);grid-auto-flow:column;overflow-y:hidden;overflow-x:scroll}"]});let o=n;return o})();var ae=[{path:"**",component:ce}];var ue={providers:[oe(ae)]};var fe=(()=>{let n=class n{};n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=b({type:n}),n.\u0275inj=w({imports:[m]});let o=n;return o})();var me=(()=>{let n=class n{};n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=b({type:n}),n.\u0275inj=w({providers:[{provide:d,useClass:d}],imports:[m,H,k,fe]});let o=n;return o})();var de=(()=>{let n=class n{};n.\u0275fac=function(t){return new(t||n)},n.\u0275cmp=f({type:n,selectors:[["app-root"]],standalone:!0,features:[K([{provide:X,useValue:"/ui/"},{provide:d,useClass:d}]),D],decls:1,vars:0,template:function(t,i){t&1&&u(0,"router-outlet")},dependencies:[m,ie,me,k,re],styles:["[_nghost-%COMP%]{height:100%}"]});let o=n;return o})();te(de,ue).catch(o=>console.error(o));
