<template>
<div class="mb-16 mt-4 px-12 grass" color="blackwhite">
  <v-divider></v-divider>
  <h2 class='pt-3 mt-14'>커밋</h2>
  <div class="subtitle-2 text-center">
    <h4 v-if="!flag">아직 잔디를 심지 않았어요!<br>
      게시물을 작성해서 잔디를 심어보세요~
    </h4>
  </div>
  <v-sparkline
    :value="value"
    :label-size="4"
    :labels="labels"
    :gradient="gradient"
    :smooth="radius || false"
    :padding="padding"
    :line-width="width"
    :stroke-linecap="lineCap"
    :gradient-direction="gradientDirection"
    :fill="fill"
    :type="type"
    :auto-line-width="autoLineWidth"
    auto-draw
  ></v-sparkline>
</div>
</template>

<script>
import {mapState} from 'vuex'
import { showJandi } from '@/api/personal.js';
import { getuidCookie, getblogIdCookie } from '@/util/cookie.js';

  const gradients = [
    ['#222'],
    ['#42b3f4'],
    ['red', 'orange', 'yellow'],
    ['purple', 'violet'],
    ['#00c6ff', '#F0F', '#FF0'],
    ['#f72047', '#ffd200', '#1feaea'],
  ]

export default {
  name: 'grass',
  data() {
    return {
      result: [],
      labels: [],
      width: 2,
      radius: 10,
      padding: 8,
      lineCap: 'round',
      gradient: gradients[5],
      value: [],
      gradientDirection: 'top',
      gradients,
      fill: false,
      type: 'trend',
      autoLineWidth: false,
      user: {
        user:'',
        blogId: '',
      },
      flag: true,
    }
  },
  created(){
    this.initUser();
    this.jandi(); 
    //location.reload();
   // console.log('잔디 새로고침');
  },
  watch: {
    $route:function () {
       this.initUser();
       this.flag=true;
       this.jandi();
    }
  },
  computed: {
    ...mapState([ 'loggedInUserData' ])    
  },
  methods: {
    initUser(){
      this.user.user = getuidCookie();
      this.user.blogId = getblogIdCookie();
    },
    jandi(){
      // console.log('잔디실행');
       if(this.user.blogId!==this.$route.query.blogId){
        //내가 아니면 
        this.user.blogId=this.$route.query.blogId;
      }
       if(typeof this.$route.query.blogId==='undefined'){
        //나이면
        this.user.blogId=getblogIdCookie();
        //console.log("언디파인드실행")
      }
      showJandi(
      this.user.blogId,
      (response) => {
        this.result = response.data[0]
       // console.log(this.result);
        const temp=[];
       
         for(var key in this.result){
          temp.push(this.result[key]);
        }
        this.value=temp;
        if(temp.length==0) this.flag=false;

       var tmp=new Array();
       var j=0;
       for(var idx in this.result){
        if(j%30==0) {
          tmp.push(idx.substring(0,7));
        }else{
          tmp.push(' ');
        }
        j++;
       }
       this.labels=tmp;
       
      },
      (err) => {
        this.flag=false;
        console.log(err)
      }
      );
      
    }  
  }
}
</script>


<style>
.grass {
  border: 0.2px hidden grey ;
  border-radius: 10px;
}
</style>