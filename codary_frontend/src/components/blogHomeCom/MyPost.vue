<template>
  <div class="py-12">
    <v-container>
      <v-responsive class="mx-auto mb-12" width="56">
        <v-divider class="mb-1"></v-divider>
        <v-divider></v-divider>
      </v-responsive>
      <div class="subtitle-2 text-center">
        <h4 v-if="this.articles.length == 0">작성한 글이 없어요!</h4>
      </div>

      <v-row>
        <v-col
          v-for="{
            blogContentsCover,
            blogDatetime,
            blogContentsTitle,
            blogContentsLike,
            blogContentsView,
            blogContentsId,
            blogContents,
            blogId,
            profile,
            nickname,
            commentCnt,
            hashtags,
          } in articles"
          :key="blogContentsId"
          cols="12"
          md="4"
        >
          <router-link
            :to="{
              name: 'ViewPost',
              query: {
                blogId: blogId,
                blogContentsId: blogContentsId,
              },
            }"
            class="noline rounded-lg"
          >
            <v-hover v-slot="{ hover }">
              <v-card
                :elevation="hover ? 7 : 0"
                class="contentCard"
                style="height: 400px; transition: 0.3s"
              >
                <div>
                  <v-img
                    :src="blogContentsCover"
                    class="mb-4"
                    :style="hover ? { opacity: 0.4 } : { opacity: 1 }"
                    height="150px"
                    max-width="100%"
                  >
                  </v-img>

                  <div
                    v-if="hover"
                    class="d-flex align-center pl-2 white--black"
                    style="position: absolute; top: 0%"
                    transition="fade-transition"
                  >
                    <div
                      class="d-flex align-center pl-2 pr-2 white--black"
                      style="height: 150px"
                    >
                      {{ blogContents | textLengthOverCut }}
                    </div>
                  </div>
                </div>
                <!-- 텍스트 -->
                <v-sheet class="pl-5 pa-2" height="150px">
                  <h2 class="font-weight-bold mb-3 pl-0 ml-0 mr-5">
                    {{ blogContentsTitle | textLengthOverCutTitle }}
                  </h2>
                  <h4 class="mb-2 pl-1">
                    <v-chip
                      v-for="(hashtag, index) in hashtags"
                      :key="index"
                      outlined
                      small
                      class="mr-2"
                      >{{ hashtag.hashtagContent }}</v-chip
                    >
                  </h4>
                </v-sheet>
                 <div class="d-flex align-end mr-3 px-3 mt-6 justify-space-between">
                  
                  <div class="d-flex align-center pl-1 mb-2">
                    <v-img
                      :src="profile"
                      class="mr-3"
                      height="30"
                      max-width="30px"
                      style="border-radius: 15px"
                    ></v-img>
                    <span>
                      {{ nickname }}
                    </span>
                  </div>
                  <div class='text-right'>
                    <span class="font-weight-light">
                      <font-awesome-icon :icon="['fas', 'comment-dots']" />
                      {{ commentCnt }}
                      <font-awesome-icon :icon="['fas', 'heart']" class="ml-2" />
                      {{ blogContentsLike }}
                      <font-awesome-icon :icon="['fas', 'eye']" class="ml-2" />
                      {{ blogContentsView }}
                    </span>
                    <div
                      class="font-weight-light justify-end"
                      v-text="blogDatetime"
                    ></div>
                  </div>
                </div>
                <!-- <v-btn class="ml-n4 font-weight-black" text>
              Continue Reading
            </v-btn> -->
              </v-card>
            </v-hover>
          </router-link>
        </v-col>
      </v-row>
    </v-container>
    <div class='py-16'></div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import { personalList } from "@/api/personal.js";
import { getuidCookie, getblogIdCookie } from "@/util/cookie.js";

export default {
  name: "MyPost",

  data() {
    return {
      articles: [],
      user: {
        user: "",
        blogId: "",
      },
    };
  },
  computed: {
    ...mapState(["loggedInUserData"]),
  },
  created() {
    this.initUser();
    this.mypost();
  },
  methods: {
    initUser() {
      this.user.user = getuidCookie();
      this.user.blogId = getblogIdCookie();
    },

    mypost() {
      // console.log("Mypost받은값 "+this.$route.query.blogId);
      // console.log("Mypost나 "+this.user.blogId);

      if (this.user.blogId !== this.$route.query.blogId) {
        //내가 아니면
        this.user.blogId = this.$route.query.blogId;
      }
      if (typeof this.$route.query.blogId === "undefined") {
        //나이면
        this.user.blogId = getblogIdCookie();
      }

      personalList(
        this.user.blogId,
        (response) => {
          //console.log(response)
          this.articles = response.data;
        },
        (err) => {
          console.log(err);
        }
      );
    },
  },
  filters: {
    textLengthOverCut(txt, len, lastTxt) {
      if (len == "" || len == null) {
        len = 100;
      }
      if (lastTxt == "" || lastTxt == null) {
        lastTxt = "...";
      }
      if (txt.length > len) {
        txt = txt.substr(0, len) + lastTxt;
      }
      return txt;
    },
    textLengthOverCutTitle(txt, len, lastTxt) {
      if (len == "" || len == null) {
        len = 27;
      }
      if (lastTxt == "" || lastTxt == null) {
        lastTxt = "...";
      }
      if (txt.length > len) {
        txt = txt.substr(0, len) + lastTxt;
      }
      return txt;
    },
  },
};
</script>

<style>
.contentCard:hover {
  transition-duration: all 1s ease;
  transform: translateY(-8px), scale(8%);
}
</style>