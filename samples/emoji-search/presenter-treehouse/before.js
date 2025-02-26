(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js', './redwood-redwood-runtime.js', './redwood-redwood-compose.js', './compose-multiplatform-core-compose-runtime-runtime.js', './compose-multiplatform-core-compose-runtime-runtime-saveable.js', './redwood-redwood-lazylayout-compose.js', './redwood-redwood-layout-api.js', './redwood-samples-emoji-search-values.js', './kotlinx-coroutines-core.js', './redwood-samples-emoji-search-schema-compose.js', './redwood-redwood-layout-compose.js', './kotlinx-serialization-kotlinx-serialization-json.js', './kotlinx-serialization-kotlinx-serialization-core.js', './redwood-redwood-treehouse-guest-compose.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./redwood-redwood-runtime.js'), require('./redwood-redwood-compose.js'), require('./compose-multiplatform-core-compose-runtime-runtime.js'), require('./compose-multiplatform-core-compose-runtime-runtime-saveable.js'), require('./redwood-redwood-lazylayout-compose.js'), require('./redwood-redwood-layout-api.js'), require('./redwood-samples-emoji-search-values.js'), require('./kotlinx-coroutines-core.js'), require('./redwood-samples-emoji-search-schema-compose.js'), require('./redwood-redwood-layout-compose.js'), require('./kotlinx-serialization-kotlinx-serialization-json.js'), require('./kotlinx-serialization-kotlinx-serialization-core.js'), require('./redwood-redwood-treehouse-guest-compose.js'));
  else {
    if (typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-runtime'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-runtime' was not found. Please, check whether 'redwood-redwood-runtime' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-compose'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-compose' was not found. Please, check whether 'redwood-redwood-compose' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['compose-multiplatform-core-compose-runtime-runtime'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'compose-multiplatform-core-compose-runtime-runtime' was not found. Please, check whether 'compose-multiplatform-core-compose-runtime-runtime' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['compose-multiplatform-core-compose-runtime-runtime-saveable'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'compose-multiplatform-core-compose-runtime-runtime-saveable' was not found. Please, check whether 'compose-multiplatform-core-compose-runtime-runtime-saveable' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-lazylayout-compose'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-lazylayout-compose' was not found. Please, check whether 'redwood-redwood-lazylayout-compose' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-layout-api'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-layout-api' was not found. Please, check whether 'redwood-redwood-layout-api' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-samples-emoji-search-values'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-samples-emoji-search-values' was not found. Please, check whether 'redwood-samples-emoji-search-values' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['kotlinx-coroutines-core'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'kotlinx-coroutines-core' was not found. Please, check whether 'kotlinx-coroutines-core' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-samples-emoji-search-schema-compose'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-samples-emoji-search-schema-compose' was not found. Please, check whether 'redwood-samples-emoji-search-schema-compose' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-layout-compose'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-layout-compose' was not found. Please, check whether 'redwood-redwood-layout-compose' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['kotlinx-serialization-kotlinx-serialization-json'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'kotlinx-serialization-kotlinx-serialization-json' was not found. Please, check whether 'kotlinx-serialization-kotlinx-serialization-json' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['kotlinx-serialization-kotlinx-serialization-core'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'kotlinx-serialization-kotlinx-serialization-core' was not found. Please, check whether 'kotlinx-serialization-kotlinx-serialization-core' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    if (typeof globalThis['redwood-redwood-treehouse-guest-compose'] === 'undefined') {
      throw new Error("Error loading module 'redwood-samples-emoji-search-presenter'. Its dependency 'redwood-redwood-treehouse-guest-compose' was not found. Please, check whether 'redwood-redwood-treehouse-guest-compose' is loaded prior to 'redwood-samples-emoji-search-presenter'.");
    }
    globalThis['redwood-samples-emoji-search-presenter'] = factory(typeof globalThis['redwood-samples-emoji-search-presenter'] === 'undefined' ? {} : globalThis['redwood-samples-emoji-search-presenter'], globalThis['kotlin-kotlin-stdlib'], globalThis['redwood-redwood-runtime'], globalThis['redwood-redwood-compose'], globalThis['compose-multiplatform-core-compose-runtime-runtime'], globalThis['compose-multiplatform-core-compose-runtime-runtime-saveable'], globalThis['redwood-redwood-lazylayout-compose'], globalThis['redwood-redwood-layout-api'], globalThis['redwood-samples-emoji-search-values'], globalThis['kotlinx-coroutines-core'], globalThis['redwood-samples-emoji-search-schema-compose'], globalThis['redwood-redwood-layout-compose'], globalThis['kotlinx-serialization-kotlinx-serialization-json'], globalThis['kotlinx-serialization-kotlinx-serialization-core'], globalThis['redwood-redwood-treehouse-guest-compose']);
  }
}(function (_, kotlin_kotlin, kotlin_app_cash_redwood_redwood_runtime, kotlin_app_cash_redwood_redwood_compose, kotlin_org_jetbrains_compose_runtime_runtime, kotlin_org_jetbrains_compose_runtime_runtime_saveable, kotlin_app_cash_redwood_redwood_lazylayout_compose, kotlin_app_cash_redwood_redwood_layout_api, kotlin_app_cash_redwood_values, kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core, kotlin_app_cash_redwood_compose, kotlin_app_cash_redwood_redwood_layout_compose, kotlin_org_jetbrains_kotlinx_kotlinx_serialization_json, kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core, kotlin_app_cash_redwood_redwood_treehouse_guest_compose) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var protoOf = kotlin_kotlin.$_$.ua;
  var getStringHashCode = kotlin_kotlin.$_$.r9;
  var THROW_CCE = kotlin_kotlin.$_$.od;
  var initMetadataForClass = kotlin_kotlin.$_$.t9;
  var initMetadataForInterface = kotlin_kotlin.$_$.x9;
  var VOID = kotlin_kotlin.$_$.g;
  var Unit_instance = kotlin_kotlin.$_$.s4;
  var Companion_instance = kotlin_app_cash_redwood_redwood_runtime.$_$.r;
  var get_LocalUiConfiguration = kotlin_app_cash_redwood_redwood_compose.$_$.b;
  var sourceInformationMarkerStart = kotlin_org_jetbrains_compose_runtime_runtime.$_$.h1;
  var sourceInformationMarkerEnd = kotlin_org_jetbrains_compose_runtime_runtime.$_$.g1;
  var traceEventStart = kotlin_org_jetbrains_compose_runtime_runtime.$_$.l1;
  var isTraceInProgress = kotlin_org_jetbrains_compose_runtime_runtime.$_$.z;
  var createCompositionCoroutineScope = kotlin_org_jetbrains_compose_runtime_runtime.$_$.u;
  var CompositionScopedCoroutineScopeCanceller = kotlin_org_jetbrains_compose_runtime_runtime.$_$.j;
  var Companion_getInstance = kotlin_org_jetbrains_compose_runtime_runtime.$_$.r1;
  var mutableStateListOf = kotlin_org_jetbrains_compose_runtime_runtime.$_$.b1;
  var mutableIntStateOf = kotlin_org_jetbrains_compose_runtime_runtime.$_$.a1;
  var println = kotlin_kotlin.$_$.z8;
  var mutableStateOf = kotlin_org_jetbrains_compose_runtime_runtime.$_$.c1;
  var rememberSaveable = kotlin_org_jetbrains_compose_runtime_runtime_saveable.$_$.f;
  var rememberLazyListState = kotlin_app_cash_redwood_redwood_lazylayout_compose.$_$.c;
  var LaunchedEffect = kotlin_org_jetbrains_compose_runtime_runtime.$_$.l;
  var split = kotlin_kotlin.$_$.bc;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.m;
  var Collection = kotlin_kotlin.$_$.c5;
  var isInterface = kotlin_kotlin.$_$.ja;
  var contains = kotlin_kotlin.$_$.qb;
  var derivedStateOf = kotlin_org_jetbrains_compose_runtime_runtime.$_$.x;
  var Companion_getInstance_0 = kotlin_app_cash_redwood_redwood_layout_api.$_$.e;
  var Companion_getInstance_1 = kotlin_app_cash_redwood_redwood_layout_api.$_$.f;
  var Margin = kotlin_app_cash_redwood_redwood_runtime.$_$.c;
  var TextFieldState = kotlin_app_cash_redwood_values.$_$.a;
  var launch = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.y;
  var RuntimeException_init_$Create$ = kotlin_kotlin.$_$.d2;
  var TextInput = kotlin_app_cash_redwood_compose.$_$.b;
  var _UInt___init__impl__l7qpdl = kotlin_kotlin.$_$.b3;
  var reuse = kotlin_app_cash_redwood_compose.$_$.d;
  var traceEventEnd = kotlin_org_jetbrains_compose_runtime_runtime.$_$.k1;
  var composableLambdaInstance = kotlin_org_jetbrains_compose_runtime_runtime.$_$.b;
  var _Dp___init__impl__ms3zkb = kotlin_app_cash_redwood_redwood_runtime.$_$.l;
  var Spacer = kotlin_app_cash_redwood_redwood_layout_compose.$_$.c;
  var app_cash_redwood_lazylayout_compose_LazyListState$stableprop_getter = kotlin_app_cash_redwood_redwood_lazylayout_compose.$_$.b;
  var LazyColumn = kotlin_app_cash_redwood_redwood_lazylayout_compose.$_$.a;
  var rememberComposableLambda = kotlin_org_jetbrains_compose_runtime_runtime.$_$.c;
  var Column = kotlin_app_cash_redwood_redwood_layout_compose.$_$.a;
  var updateChangedFlags = kotlin_org_jetbrains_compose_runtime_runtime.$_$.m1;
  var initMetadataForObject = kotlin_kotlin.$_$.z9;
  var Companion_getInstance_2 = kotlin_app_cash_redwood_redwood_layout_api.$_$.g;
  var Margin_0 = kotlin_app_cash_redwood_redwood_runtime.$_$.b;
  var Image = kotlin_app_cash_redwood_compose.$_$.a;
  var Text = kotlin_app_cash_redwood_compose.$_$.c;
  var Row = kotlin_app_cash_redwood_redwood_layout_compose.$_$.b;
  var KMutableProperty0 = kotlin_kotlin.$_$.ib;
  var THROW_ISE = kotlin_kotlin.$_$.pd;
  var getLocalDelegateReference = kotlin_kotlin.$_$.o9;
  var KProperty0 = kotlin_kotlin.$_$.kb;
  var EmptyCoroutineContext_getInstance = kotlin_kotlin.$_$.a4;
  var Saver = kotlin_org_jetbrains_compose_runtime_runtime_saveable.$_$.d;
  var CoroutineImpl = kotlin_kotlin.$_$.x8;
  var CoroutineScope = kotlin_org_jetbrains_kotlinx_kotlinx_coroutines_core.$_$.q;
  var initMetadataForLambda = kotlin_kotlin.$_$.y9;
  var to = kotlin_kotlin.$_$.xe;
  var mapOf = kotlin_kotlin.$_$.f7;
  var get_COROUTINE_SUSPENDED = kotlin_kotlin.$_$.i8;
  var Default_getInstance = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_json.$_$.a;
  var KtMap = kotlin_kotlin.$_$.j5;
  var getKClass = kotlin_kotlin.$_$.e;
  var PrimitiveClasses_getInstance = kotlin_kotlin.$_$.i4;
  var arrayOf = kotlin_kotlin.$_$.ce;
  var createKType = kotlin_kotlin.$_$.b;
  var createInvariantKTypeProjection = kotlin_kotlin.$_$.a;
  var serializer = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.z2;
  var KSerializer = kotlin_org_jetbrains_kotlinx_kotlinx_serialization_core.$_$.q2;
  var ArrayList_init_$Create$_0 = kotlin_kotlin.$_$.l;
  var ConsumeInsets = kotlin_app_cash_redwood_redwood_compose.$_$.a;
  var close = kotlin_app_cash_redwood_redwood_treehouse_guest_compose.$_$.a;
  var TreehouseUi = kotlin_app_cash_redwood_redwood_treehouse_guest_compose.$_$.b;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(EmojiImage, 'EmojiImage');
  initMetadataForInterface(HttpClient, 'HttpClient', VOID, VOID, VOID, [2]);
  initMetadataForObject(ComposableSingletons$EmojiSearchKt, 'ComposableSingletons$EmojiSearchKt');
  initMetadataForClass(EmojiSearch$searchTermSaver$1, VOID, VOID, VOID, [Saver]);
  initMetadataForLambda(EmojiSearch$slambda, CoroutineImpl, VOID, [1]);
  initMetadataForLambda(EmojiSearch$slambda_1, CoroutineImpl, VOID, [1]);
  initMetadataForLambda(EmojiSearch$lambda$lambda$slambda, CoroutineImpl, VOID, [1]);
  initMetadataForClass(EmojiSearchTreehouseUi, 'EmojiSearchTreehouseUi', VOID, VOID, [TreehouseUi]);
  //endregion
  function get_loadingEmojiImage() {
    _init_properties_EmojiSearch_kt__h8cahw();
    return loadingEmojiImage;
  }
  var loadingEmojiImage;
  var com_example_redwood_emojisearch_presenter_EmojiImage$stable;
  function EmojiImage(label, url) {
    this.g50_1 = label;
    this.h50_1 = url;
  }
  protoOf(EmojiImage).toString = function () {
    return 'EmojiImage(label=' + this.g50_1 + ', url=' + this.h50_1 + ')';
  };
  protoOf(EmojiImage).hashCode = function () {
    var result = getStringHashCode(this.g50_1);
    result = imul(result, 31) + getStringHashCode(this.h50_1) | 0;
    return result;
  };
  protoOf(EmojiImage).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof EmojiImage))
      return false;
    var tmp0_other_with_cast = other instanceof EmojiImage ? other : THROW_CCE();
    if (!(this.g50_1 === tmp0_other_with_cast.g50_1))
      return false;
    if (!(this.h50_1 === tmp0_other_with_cast.h50_1))
      return false;
    return true;
  };
  function HttpClient() {
  }
  function EmojiSearch(httpClient, navigator, modifier, viewInsets, $composer, $changed, $default) {
    _init_properties_EmojiSearch_kt__h8cahw();
    var modifier_0 = {_v: modifier};
    var viewInsets_0 = {_v: viewInsets};
    var $composer_0 = $composer;
    $composer_0 = $composer_0.o2w(163827474);
    var $dirty = $changed;
    if (!(($default & 1) === 0))
      $dirty = $dirty | 6;
    else if (($changed & 6) === 0)
      $dirty = $dirty | ((($changed & 8) === 0 ? $composer_0.m2l(httpClient) : $composer_0.e2v(httpClient)) ? 4 : 2);
    if (!(($default & 2) === 0))
      $dirty = $dirty | 48;
    else if (($changed & 48) === 0)
      $dirty = $dirty | ((($changed & 64) === 0 ? $composer_0.m2l(navigator) : $composer_0.e2v(navigator)) ? 32 : 16);
    if (!(($default & 4) === 0))
      $dirty = $dirty | 384;
    else if (($changed & 384) === 0)
      $dirty = $dirty | ($composer_0.m2l(modifier_0._v) ? 256 : 128);
    if (($changed & 3072) === 0)
      $dirty = $dirty | (($default & 8) === 0 && $composer_0.m2l(viewInsets_0._v) ? 2048 : 1024);
    if (!(($dirty & 1171) === 1170) || !$composer_0.l2r()) {
      $composer_0.g2u();
      if (($changed & 1) === 0 || $composer_0.k2u()) {
        if (!(($default & 4) === 0)) {
          modifier_0._v = Companion_instance;
        }
        if (!(($default & 8) === 0)) {
          var tmp0 = get_LocalUiConfiguration();
          // Inline function 'androidx.compose.runtime.CompositionLocal.current' call
          var $composer_1 = $composer_0;
          sourceInformationMarkerStart($composer_1, 2023513938, 'CC:CompositionLocal.kt#9igjgp');
          var tmp0_0 = $composer_1.k2w(tmp0);
          sourceInformationMarkerEnd($composer_1);
          viewInsets_0._v = tmp0_0.u3y_1;
          $dirty = $dirty & -7169;
        }
      } else {
        $composer_0.m2o();
        if (!(($default & 8) === 0))
          $dirty = $dirty & -7169;
      }
      $composer_0.h2u();
      if (isTraceInProgress()) {
        traceEventStart(163827474, $dirty, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearch (EmojiSearch.kt:75)');
      }
      // Inline function 'androidx.compose.runtime.rememberCoroutineScope' call
      var getContext = null;
      var $composer_2 = $composer_0;
      sourceInformationMarkerStart($composer_2, 773894976, 'CC(rememberCoroutineScope)482@20254L144:Effects.kt#9igjgp');
      if (!((1 & 1) === 0)) {
        getContext = EmojiSearch$lambda_6;
      }
      var composer = $composer_2;
      sourceInformationMarkerStart($composer_2, -954370320, 'CC(remember):Effects.kt#9igjgp');
      // Inline function 'androidx.compose.runtime.cache' call
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it = $composer_2.t2v();
      var tmp;
      if (false || it === Companion_getInstance().v2o_1) {
        // Inline function 'androidx.compose.runtime.rememberCoroutineScope.<anonymous>' call
        var value = new CompositionScopedCoroutineScopeCanceller(createCompositionCoroutineScope(getContext(), composer));
        $composer_2.e2w(value);
        tmp = value;
      } else {
        tmp = it;
      }
      var tmp_0 = tmp;
      var tmp1_group = (tmp_0 == null ? true : !(tmp_0 == null)) ? tmp_0 : THROW_CCE();
      sourceInformationMarkerEnd($composer_2);
      var wrapper = tmp1_group;
      var tmp0_1 = wrapper.r33_1;
      sourceInformationMarkerEnd($composer_2);
      var scope = tmp0_1;
      $composer_0.d2u(-1261094284);
      // Inline function 'androidx.compose.runtime.cache' call
      var this_0 = $composer_0;
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_0 = this_0.t2v();
      var tmp_1;
      if (false || it_0 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_0 = mutableStateListOf();
        this_0.e2w(value_0);
        tmp_1 = value_0;
      } else {
        tmp_1 = it_0;
      }
      var tmp_2 = tmp_1;
      var tmp0_group = (tmp_2 == null ? true : !(tmp_2 == null)) ? tmp_2 : THROW_CCE();
      $composer_0.f2u();
      var allEmojis = tmp0_group;
      $composer_0.d2u(-1261089208);
      // Inline function 'androidx.compose.runtime.cache' call
      var this_1 = $composer_0;
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_1 = this_1.t2v();
      var tmp_3;
      if (false || it_1 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_1 = mutableIntStateOf(0);
        this_1.e2w(value_1);
        tmp_3 = value_1;
      } else {
        tmp_3 = it_1;
      }
      var tmp_4 = tmp_3;
      var tmp1_group_0 = (tmp_4 == null ? true : !(tmp_4 == null)) ? tmp_4 : THROW_CCE();
      $composer_0.f2u();
      var refreshSignal$delegate = tmp1_group_0;
      println('SSSS');
      $composer_0.d2u(-1261086903);
      // Inline function 'androidx.compose.runtime.cache' call
      var this_2 = $composer_0;
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_2 = this_2.t2v();
      var tmp_5;
      if (false || it_2 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_2 = mutableStateOf(false);
        this_2.e2w(value_2);
        tmp_5 = value_2;
      } else {
        tmp_5 = it_2;
      }
      var tmp_6 = tmp_5;
      var tmp2_group = (tmp_6 == null ? true : !(tmp_6 == null)) ? tmp_6 : THROW_CCE();
      $composer_0.f2u();
      var refreshing$delegate = tmp2_group;
      var searchTermSaver = new EmojiSearch$searchTermSaver$1();
      $composer_0.d2u(-1261077107);
      // Inline function 'androidx.compose.runtime.cache' call
      var this_3 = $composer_0;
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_3 = this_3.t2v();
      var tmp_7;
      if (false || it_3 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_3 = EmojiSearch$lambda_7;
        this_3.e2w(value_3);
        tmp_7 = value_3;
      } else {
        tmp_7 = it_3;
      }
      var tmp_8 = tmp_7;
      var tmp3_group = (tmp_8 == null ? true : !(tmp_8 == null)) ? tmp_8 : THROW_CCE();
      $composer_0.f2u();
      var searchTerm$delegate = rememberSaveable([], searchTermSaver, null, tmp3_group, $composer_0, 3072, 4);
      var lazyListState = rememberLazyListState(null, $composer_0, 0, 1);
      var tmp_9 = EmojiSearch$lambda_3(searchTerm$delegate);
      $composer_0.d2u(-1261073371);
      var tmp15 = $composer_0;
      // Inline function 'androidx.compose.runtime.cache' call
      var invalid = $composer_0.e2v(lazyListState);
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_4 = tmp15.t2v();
      var tmp_10;
      if (invalid || it_4 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_4 = EmojiSearch$slambda_0(lazyListState, null);
        tmp15.e2w(value_4);
        tmp_10 = value_4;
      } else {
        tmp_10 = it_4;
      }
      var tmp_11 = tmp_10;
      var tmp4_group = (tmp_11 == null ? true : !(tmp_11 == null)) ? tmp_11 : THROW_CCE();
      $composer_0.f2u();
      LaunchedEffect(tmp_9, tmp4_group, $composer_0, 0);
      var tmp_12 = EmojiSearch$lambda(refreshSignal$delegate);
      $composer_0.d2u(-1261069889);
      var tmp17 = $composer_0;
      // Inline function 'androidx.compose.runtime.cache' call
      var invalid_0 = ($dirty & 14) === 4 || (!(($dirty & 8) === 0) && $composer_0.e2v(httpClient));
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_5 = tmp17.t2v();
      var tmp_13;
      if (invalid_0 || it_5 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_5 = EmojiSearch$slambda_2(httpClient, allEmojis, refreshing$delegate, null);
        tmp17.e2w(value_5);
        tmp_13 = value_5;
      } else {
        tmp_13 = it_5;
      }
      var tmp_14 = tmp_13;
      var tmp5_group = (tmp_14 == null ? true : !(tmp_14 == null)) ? tmp_14 : THROW_CCE();
      $composer_0.f2u();
      LaunchedEffect(tmp_12, tmp5_group, $composer_0, 0);
      $composer_0.d2u(-1261054219);
      // Inline function 'androidx.compose.runtime.cache' call
      var this_4 = $composer_0;
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_6 = this_4.t2v();
      var tmp_15;
      if (false || it_6 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
        var value_6 = derivedStateOf(function () {
          var searchTerms = split(EmojiSearch$lambda_3(searchTerm$delegate).n4z_1, [' ']);
          // Inline function 'kotlin.collections.filter' call
          var tmp0 = allEmojis;
          // Inline function 'kotlin.collections.filterTo' call
          var destination = ArrayList_init_$Create$();
          var _iterator__ex2g4s = tmp0.j();
          while (_iterator__ex2g4s.k()) {
            var element = _iterator__ex2g4s.l();
            // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>' call
            var tmp$ret$30;
            $l$block_0: {
              // Inline function 'kotlin.collections.all' call
              var tmp;
              if (isInterface(searchTerms, Collection)) {
                tmp = searchTerms.q();
              } else {
                tmp = false;
              }
              if (tmp) {
                tmp$ret$30 = true;
                break $l$block_0;
              }
              var _iterator__ex2g4s_0 = searchTerms.j();
              while (_iterator__ex2g4s_0.k()) {
                var element_0 = _iterator__ex2g4s_0.l();
                // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>.<anonymous>' call
                if (!contains(element.g50_1, element_0, true)) {
                  tmp$ret$30 = false;
                  break $l$block_0;
                }
              }
              tmp$ret$30 = true;
            }
            if (tmp$ret$30) {
              destination.e(element);
            }
          }
          return destination;
        });
        this_4.e2w(value_6);
        tmp_15 = value_6;
      } else {
        tmp_15 = it_6;
      }
      var tmp_16 = tmp_15;
      var tmp6_group = (tmp_16 == null ? true : !(tmp_16 == null)) ? tmp_16 : THROW_CCE();
      $composer_0.f2u();
      var filteredEmojis$delegate = tmp6_group;
      var tmp0_width = Companion_getInstance_0().j4v_1;
      var tmp1_height = Companion_getInstance_0().j4v_1;
      var tmp2_horizontalAlignment = Companion_getInstance_1().d4v_1;
      var tmp3_margin = new Margin(viewInsets_0._v.e3y_1, viewInsets_0._v.f3y_1, viewInsets_0._v.g3y_1);
      var tmp_17 = modifier_0._v;
      // Inline function 'kotlin.run' call
      // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>' call
      var dispatchReceiver = rememberComposableLambda(-967289331, true, function ($this$Column, $composer, $changed) {
        var $composer_0 = $composer;
        var $dirty = $changed;
        var tmp;
        if (($changed & 6) === 0) {
          $dirty = $dirty | ($composer_0.m2l($this$Column) ? 4 : 2);
          tmp = Unit_instance;
        }
        var tmp_0;
        if (!(($dirty & 19) === 18) || !$composer_0.l2r()) {
          if (isTraceInProgress()) {
            traceEventStart(-967289331, $dirty, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous> (EmojiSearch.kt:130)');
          }
          var tmp_1 = new TextFieldState(EmojiSearch$lambda_3(searchTerm$delegate).n4z_1);
          $composer_0.d2u(-1112667019);
          // Inline function 'androidx.compose.runtime.cache' call
          var invalid = !!($composer_0.e2v(scope) | $composer_0.m2l(searchTerm$delegate));
          // Inline function 'kotlin.let' call
          // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
          var it = $composer_0.t2v();
          var tmp_2;
          if (invalid || it === Companion_getInstance().v2o_1) {
            // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>' call
            var value = function (textFieldState) {
              var tmp0_subject = textFieldState.n4z_1;
              var tmp;
              if (tmp0_subject === 'crash') {
                throw RuntimeException_init_$Create$('boom!');
              } else if (tmp0_subject === 'async') {
                launch(scope, VOID, VOID, EmojiSearch$lambda$lambda$slambda_0(null));
                tmp = Unit_instance;
              }
              EmojiSearch$lambda_4(searchTerm$delegate, textFieldState);
              return Unit_instance;
            };
            $composer_0.e2w(value);
            tmp_2 = value;
          } else {
            tmp_2 = it;
          }
          var tmp_3 = tmp_2;
          var tmp0_group = (tmp_3 == null ? true : !(tmp_3 == null)) ? tmp_3 : THROW_CCE();
          $composer_0.f2u();
          TextInput(tmp_1, 'Search', tmp0_group, null, $composer_0, 48, 8);
          var tmp0_refreshing = EmojiSearch$lambda_1(refreshing$delegate);
          var tmp1_width = Companion_getInstance_0().j4v_1;
          var tmp2_modifier = $this$Column.s4w(Companion_instance, 1.0);
          $composer_0.d2u(-1112653287);
          // Inline function 'androidx.compose.runtime.cache' call
          // Inline function 'kotlin.let' call
          // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
          var it_0 = $composer_0.t2v();
          var tmp_4;
          if (false || it_0 === Companion_getInstance().v2o_1) {
            // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>' call
            var value_0 = function () {
              var _unary__edvuaz = EmojiSearch$lambda(refreshSignal$delegate);
              EmojiSearch$lambda_0(refreshSignal$delegate, _unary__edvuaz + 1 | 0);
              return Unit_instance;
            };
            $composer_0.e2w(value_0);
            tmp_4 = value_0;
          } else {
            tmp_4 = it_0;
          }
          var tmp_5 = tmp_4;
          var tmp1_group = (tmp_5 == null ? true : !(tmp_5 == null)) ? tmp_5 : THROW_CCE();
          $composer_0.f2u();
          var tmp_6 = ComposableSingletons$EmojiSearchKt_getInstance().j50_1;
          var tmp_7 = _UInt___init__impl__l7qpdl(0);
          $composer_0.d2u(-1112645179);
          // Inline function 'androidx.compose.runtime.cache' call
          var invalid_0 = !!($composer_0.e2v(navigator) | $composer_0.m2l(viewInsets_0._v));
          // Inline function 'kotlin.let' call
          // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
          var it_1 = $composer_0.t2v();
          var tmp_8;
          if (invalid_0 || it_1 === Companion_getInstance().v2o_1) {
            // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>' call
            var value_1 = function ($this$LazyColumn) {
              // Inline function 'app.cash.redwood.lazylayout.compose.items' call
              var items = EmojiSearch$lambda_5(filteredEmojis$delegate);
              var tmp = items.m();
              $this$LazyColumn.i4y(tmp, ComposableLambda$invoke$ref_0(composableLambdaInstance(1764560922, true, function (it, $composer, $changed) {
                var $composer_0 = $composer;
                var $dirty = $changed;
                var tmp;
                if (($changed & 6) === 0) {
                  $dirty = $dirty | ($composer_0.h2v(it) ? 4 : 2);
                  tmp = Unit_instance;
                }
                var tmp_0;
                if (!(($dirty & 19) === 18) || !$composer_0.l2r()) {
                  if (isTraceInProgress()) {
                    traceEventStart(1764560922, $dirty, -1, 'app.cash.redwood.lazylayout.compose.items.<anonymous> (LazyDsl.kt:60)');
                  }
                  // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>' call
                  var image = items.p(it);
                  var $composer_1 = $composer_0;
                  $composer_1.d2u(-518369920);
                  var tmp0_modifier = reuse(Companion_instance);
                  $composer_1.d2u(1507301977);
                  // Inline function 'androidx.compose.runtime.cache' call
                  var invalid = !!($composer_1.e2v(navigator) | ((0 & 14 ^ 6) > 4 && $composer_1.m2l(image) || (0 & 6) === 4));
                  // Inline function 'kotlin.let' call
                  // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
                  var it_0 = $composer_1.t2v();
                  var tmp_1;
                  if (invalid || it_0 === Companion_getInstance().v2o_1) {
                    // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>' call
                    var value = function () {
                      navigator.k50(image.h50_1);
                      return Unit_instance;
                    };
                    $composer_1.e2w(value);
                    tmp_1 = value;
                  } else {
                    tmp_1 = it_0;
                  }
                  var tmp_2 = tmp_1;
                  var tmp0_group = (tmp_2 == null ? true : !(tmp_2 == null)) ? tmp_2 : THROW_CCE();
                  $composer_1.f2u();
                  Item(image, tmp0_modifier, tmp0_group, $composer_1, 48 | 14 & 0, 0);
                  $composer_1.f2u();
                  var tmp_3;
                  if (isTraceInProgress()) {
                    traceEventEnd();
                    tmp_3 = Unit_instance;
                  }
                  tmp_0 = tmp_3;
                } else {
                  $composer_0.m2o();
                  tmp_0 = Unit_instance;
                }
                return Unit_instance;
              })));
              $this$LazyColumn.n4y(ComposableLambda$invoke$ref_1(composableLambdaInstance(1003431207, true, function ($composer, $changed) {
                var $composer_0 = $composer;
                var tmp;
                if (!(($changed & 3) === 2) || !$composer_0.l2r()) {
                  if (isTraceInProgress()) {
                    traceEventStart(1003431207, $changed, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>.<anonymous>.<anonymous> (EmojiSearch.kt:171)');
                  }
                  var tmp_0 = _Dp___init__impl__ms3zkb(0.0);
                  var tmp_1 = viewInsets_0._v.h3y_1;
                  Spacer(tmp_0, tmp_1, null, $composer_0, 0, 5);
                  var tmp_2;
                  if (isTraceInProgress()) {
                    traceEventEnd();
                    tmp_2 = Unit_instance;
                  }
                  tmp = tmp_2;
                } else {
                  $composer_0.m2o();
                  tmp = Unit_instance;
                }
                return Unit_instance;
              })));
              return Unit_instance;
            };
            $composer_0.e2w(value_1);
            tmp_8 = value_1;
          } else {
            tmp_8 = it_1;
          }
          var tmp_9 = tmp_8;
          var tmp2_group = (tmp_9 == null ? true : !(tmp_9 == null)) ? tmp_9 : THROW_CCE();
          $composer_0.f2u();
          LazyColumn(tmp0_refreshing, tmp1_group, tmp_6, tmp2_modifier, lazyListState, tmp1_width, null, null, null, tmp_7, tmp2_group, $composer_0, 432 | app_cash_redwood_lazylayout_compose_LazyListState$stableprop_getter() << 12, 0, 960);
          var tmp_10;
          if (isTraceInProgress()) {
            traceEventEnd();
            tmp_10 = Unit_instance;
          }
          tmp_0 = tmp_10;
        } else {
          $composer_0.m2o();
          tmp_0 = Unit_instance;
        }
        return Unit_instance;
      }, $composer_0, 54);
      // Inline function 'androidx.compose.runtime.remember' call
      var $composer_3 = $composer_0;
      sourceInformationMarkerStart($composer_3, 1157296644, 'CC(remember)P(1):Composables.kt#9igjgp');
      // Inline function 'androidx.compose.runtime.cache' call
      var invalid_1 = $composer_3.m2l(dispatchReceiver);
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_7 = $composer_3.t2v();
      var tmp_18;
      if (invalid_1 || it_7 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearch.<anonymous>.<anonymous>' call
        var value_7 = ComposableLambda$invoke$ref_2(dispatchReceiver);
        $composer_3.e2w(value_7);
        tmp_18 = value_7;
      } else {
        tmp_18 = it_7;
      }
      var tmp_19 = tmp_18;
      var tmp0_2 = (tmp_19 == null ? true : !(tmp_19 == null)) ? tmp_19 : THROW_CCE();
      sourceInformationMarkerEnd($composer_3);
      Column(tmp0_width, tmp1_height, tmp3_margin, null, tmp2_horizontalAlignment, null, null, tmp_17, tmp0_2, $composer_0, 100663296 | 29360128 & $dirty << 15, 104);
      if (isTraceInProgress()) {
        traceEventEnd();
      }
    } else {
      $composer_0.m2o();
    }
    var tmp7_safe_receiver = $composer_0.p2w();
    if (tmp7_safe_receiver == null)
      null;
    else {
      tmp7_safe_receiver.c32(function ($composer, $force) {
        EmojiSearch(httpClient, navigator, modifier_0._v, viewInsets_0._v, $composer, updateChangedFlags($changed | 1), $default);
        return Unit_instance;
      });
    }
  }
  function ComposableLambda$invoke$ref($boundThis) {
    return function (p0, p1) {
      return $boundThis.y2t(p0, p1);
    };
  }
  function ComposableSingletons$EmojiSearchKt$lambda_1$lambda_o9f7s4($composer, $changed) {
    var $composer_0 = $composer;
    if (!(($changed & 3) === 2) || !$composer_0.l2r()) {
      if (isTraceInProgress()) {
        traceEventStart(-983221114, $changed, -1, 'com.example.redwood.emojisearch.presenter.ComposableSingletons$EmojiSearchKt.lambda-1.<anonymous> (EmojiSearch.kt:155)');
      }
      var tmp = get_loadingEmojiImage();
      $composer_0.d2u(164579659);
      // Inline function 'androidx.compose.runtime.cache' call
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it = $composer_0.t2v();
      var tmp_0;
      if (false || it === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.ComposableSingletons$EmojiSearchKt.lambda-1.<anonymous>.<anonymous>' call
        var value = ComposableSingletons$EmojiSearchKt$lambda_1$lambda$lambda_xo4don;
        $composer_0.e2w(value);
        tmp_0 = value;
      } else {
        tmp_0 = it;
      }
      var tmp_1 = tmp_0;
      var tmp0_group = (tmp_1 == null ? true : !(tmp_1 == null)) ? tmp_1 : THROW_CCE();
      $composer_0.f2u();
      Item(tmp, null, tmp0_group, $composer_0, 390, 2);
      if (isTraceInProgress()) {
        traceEventEnd();
      }
    } else {
      $composer_0.m2o();
    }
    return Unit_instance;
  }
  function ComposableSingletons$EmojiSearchKt$lambda_1$lambda$lambda_xo4don() {
    return Unit_instance;
  }
  function ComposableSingletons$EmojiSearchKt() {
    ComposableSingletons$EmojiSearchKt_instance = this;
    var tmp = this;
    tmp.j50_1 = ComposableLambda$invoke$ref(composableLambdaInstance(-983221114, false, ComposableSingletons$EmojiSearchKt$lambda_1$lambda_o9f7s4));
  }
  var ComposableSingletons$EmojiSearchKt_instance;
  function ComposableSingletons$EmojiSearchKt_getInstance() {
    if (ComposableSingletons$EmojiSearchKt_instance == null)
      new ComposableSingletons$EmojiSearchKt();
    return ComposableSingletons$EmojiSearchKt_instance;
  }
  function Item(emojiImage, modifier, onClick, $composer, $changed, $default) {
    _init_properties_EmojiSearch_kt__h8cahw();
    var modifier_0 = {_v: modifier};
    var onClick_0 = {_v: onClick};
    var $composer_0 = $composer;
    $composer_0 = $composer_0.o2w(-107450788);
    var $dirty = $changed;
    if (!(($default & 1) === 0))
      $dirty = $dirty | 6;
    else if (($changed & 6) === 0)
      $dirty = $dirty | ($composer_0.m2l(emojiImage) ? 4 : 2);
    if (!(($default & 2) === 0))
      $dirty = $dirty | 48;
    else if (($changed & 48) === 0)
      $dirty = $dirty | ($composer_0.m2l(modifier_0._v) ? 32 : 16);
    if (!(($default & 4) === 0))
      $dirty = $dirty | 384;
    else if (($changed & 384) === 0)
      $dirty = $dirty | ($composer_0.e2v(onClick_0._v) ? 256 : 128);
    if (!(($dirty & 147) === 146) || !$composer_0.l2r()) {
      if (!(($default & 2) === 0)) {
        modifier_0._v = Companion_instance;
      }
      if (!(($default & 4) === 0)) {
        $composer_0.d2u(1724495772);
        // Inline function 'androidx.compose.runtime.cache' call
        var this_0 = $composer_0;
        // Inline function 'kotlin.let' call
        // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
        var it = this_0.t2v();
        var tmp;
        if (false || it === Companion_getInstance().v2o_1) {
          // Inline function 'com.example.redwood.emojisearch.presenter.Item.<anonymous>' call
          var value = Item$lambda;
          this_0.e2w(value);
          tmp = value;
        } else {
          tmp = it;
        }
        var tmp_0 = tmp;
        var tmp0_group = (tmp_0 == null ? true : !(tmp_0 == null)) ? tmp_0 : THROW_CCE();
        $composer_0.f2u();
        onClick_0._v = tmp0_group;
      }
      if (isTraceInProgress()) {
        traceEventStart(-107450788, $dirty, -1, 'com.example.redwood.emojisearch.presenter.Item (EmojiSearch.kt:182)');
      }
      var tmp0_width = Companion_getInstance_0().j4v_1;
      var tmp1_height = Companion_getInstance_0().i4v_1;
      var tmp2_verticalAlignment = Companion_getInstance_1().b4v_1;
      var tmp3_horizontalAlignment = Companion_getInstance_2().o4v_1;
      var tmp_1 = modifier_0._v;
      // Inline function 'kotlin.run' call
      // Inline function 'com.example.redwood.emojisearch.presenter.Item.<anonymous>' call
      var dispatchReceiver = rememberComposableLambda(-661806309, true, function ($this$Row, $composer, $changed) {
        var $composer_0 = $composer;
        var $dirty = $changed;
        var tmp;
        if (($changed & 6) === 0) {
          $dirty = $dirty | ($composer_0.m2l($this$Row) ? 4 : 2);
          tmp = Unit_instance;
        }
        var tmp_0;
        if (!(($dirty & 19) === 18) || !$composer_0.l2r()) {
          if (isTraceInProgress()) {
            traceEventStart(-661806309, $dirty, -1, 'com.example.redwood.emojisearch.presenter.Item.<anonymous> (EmojiSearch.kt:190)');
          }
          var tmp0_url = emojiImage.h50_1;
          var tmp_1 = Companion_instance;
          // Inline function 'app.cash.redwood.ui.dp' call
          var tmp$ret$4 = _Dp___init__impl__ms3zkb(8);
          var tmp_2 = $this$Row.w4w(tmp_1, Margin_0(tmp$ret$4));
          // Inline function 'app.cash.redwood.ui.dp' call
          var tmp_3 = _Dp___init__impl__ms3zkb(24);
          // Inline function 'app.cash.redwood.ui.dp' call
          var tmp$ret$6 = _Dp___init__impl__ms3zkb(24);
          var tmp1_modifier = $this$Row.x4w(tmp_2, tmp_3, tmp$ret$6);
          Image(tmp0_url, onClick_0._v, tmp1_modifier, $composer_0, 0, 0);
          Text(emojiImage.g50_1, null, $composer_0, 0, 2);
          var tmp_4;
          if (isTraceInProgress()) {
            traceEventEnd();
            tmp_4 = Unit_instance;
          }
          tmp_0 = tmp_4;
        } else {
          $composer_0.m2o();
          tmp_0 = Unit_instance;
        }
        return Unit_instance;
      }, $composer_0, 54);
      // Inline function 'androidx.compose.runtime.remember' call
      var $composer_1 = $composer_0;
      sourceInformationMarkerStart($composer_1, 1157296644, 'CC(remember)P(1):Composables.kt#9igjgp');
      // Inline function 'androidx.compose.runtime.cache' call
      var invalid = $composer_1.m2l(dispatchReceiver);
      // Inline function 'kotlin.let' call
      // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
      var it_0 = $composer_1.t2v();
      var tmp_2;
      if (invalid || it_0 === Companion_getInstance().v2o_1) {
        // Inline function 'com.example.redwood.emojisearch.presenter.Item.<anonymous>.<anonymous>' call
        var value_0 = ComposableLambda$invoke$ref_3(dispatchReceiver);
        $composer_1.e2w(value_0);
        tmp_2 = value_0;
      } else {
        tmp_2 = it_0;
      }
      var tmp_3 = tmp_2;
      var tmp0 = (tmp_3 == null ? true : !(tmp_3 == null)) ? tmp_3 : THROW_CCE();
      sourceInformationMarkerEnd($composer_1);
      Row(tmp0_width, tmp1_height, null, null, tmp3_horizontalAlignment, tmp2_verticalAlignment, null, tmp_1, tmp0, $composer_0, 100663296 | 29360128 & $dirty << 18, 76);
      if (isTraceInProgress()) {
        traceEventEnd();
      }
    } else {
      $composer_0.m2o();
    }
    var tmp1_safe_receiver = $composer_0.p2w();
    if (tmp1_safe_receiver == null)
      null;
    else {
      tmp1_safe_receiver.c32(function ($composer, $force) {
        Item(emojiImage, modifier_0._v, onClick_0._v, $composer, updateChangedFlags($changed | 1), $default);
        return Unit_instance;
      });
    }
  }
  function EmojiSearch$lambda($refreshSignal$delegate) {
    _init_properties_EmojiSearch_kt__h8cahw();
    // Inline function 'androidx.compose.runtime.getValue' call
    getLocalDelegateReference('refreshSignal', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    return $refreshSignal$delegate.r3d();
  }
  function EmojiSearch$lambda_0($refreshSignal$delegate, _set____db54di) {
    _init_properties_EmojiSearch_kt__h8cahw();
    getLocalDelegateReference('refreshSignal', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    $refreshSignal$delegate.q3d(_set____db54di);
    return Unit_instance;
  }
  function EmojiSearch$lambda_1($refreshing$delegate) {
    _init_properties_EmojiSearch_kt__h8cahw();
    // Inline function 'androidx.compose.runtime.getValue' call
    getLocalDelegateReference('refreshing', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    return $refreshing$delegate.y1();
  }
  function EmojiSearch$lambda_2($refreshing$delegate, _set____db54di) {
    _init_properties_EmojiSearch_kt__h8cahw();
    getLocalDelegateReference('refreshing', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    $refreshing$delegate.k2c(_set____db54di);
    return Unit_instance;
  }
  function EmojiSearch$lambda_3($searchTerm$delegate) {
    _init_properties_EmojiSearch_kt__h8cahw();
    // Inline function 'androidx.compose.runtime.getValue' call
    getLocalDelegateReference('searchTerm', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    return $searchTerm$delegate.y1();
  }
  function EmojiSearch$lambda_4($searchTerm$delegate, _set____db54di) {
    _init_properties_EmojiSearch_kt__h8cahw();
    getLocalDelegateReference('searchTerm', KMutableProperty0, true, function () {
      return THROW_ISE();
    });
    $searchTerm$delegate.k2c(_set____db54di);
    return Unit_instance;
  }
  function EmojiSearch$lambda_5($filteredEmojis$delegate) {
    _init_properties_EmojiSearch_kt__h8cahw();
    // Inline function 'androidx.compose.runtime.getValue' call
    getLocalDelegateReference('filteredEmojis', KProperty0, false, function () {
      return THROW_ISE();
    });
    return $filteredEmojis$delegate.y1();
  }
  function EmojiSearch$lambda_6() {
    _init_properties_EmojiSearch_kt__h8cahw();
    return EmptyCoroutineContext_getInstance();
  }
  function EmojiSearch$searchTermSaver$1() {
  }
  protoOf(EmojiSearch$searchTermSaver$1).l50 = function (value) {
    return new TextFieldState(value);
  };
  protoOf(EmojiSearch$searchTermSaver$1).a3z = function (value) {
    return this.l50(typeof value === 'string' ? value : THROW_CCE());
  };
  protoOf(EmojiSearch$searchTermSaver$1).m50 = function (_this__u8e3s4, value) {
    return value.n4z_1;
  };
  protoOf(EmojiSearch$searchTermSaver$1).k3z = function (_this__u8e3s4, value) {
    return this.m50(_this__u8e3s4, value instanceof TextFieldState ? value : THROW_CCE());
  };
  function EmojiSearch$lambda_7() {
    _init_properties_EmojiSearch_kt__h8cahw();
    return mutableStateOf(new TextFieldState(''));
  }
  function EmojiSearch$slambda($lazyListState, resultContinuation) {
    this.v50_1 = $lazyListState;
    CoroutineImpl.call(this, resultContinuation);
  }
  protoOf(EmojiSearch$slambda).f2e = function ($this$LaunchedEffect, $completion) {
    var tmp = this.g2e($this$LaunchedEffect, $completion);
    tmp.f9_1 = Unit_instance;
    tmp.g9_1 = null;
    return tmp.l9();
  };
  protoOf(EmojiSearch$slambda).x9 = function (p1, $completion) {
    return this.f2e((!(p1 == null) ? isInterface(p1, CoroutineScope) : false) ? p1 : THROW_CCE(), $completion);
  };
  protoOf(EmojiSearch$slambda).l9 = function () {
    var suspendResult = this.f9_1;
    $sm: do
      try {
        var tmp = this.d9_1;
        if (tmp === 0) {
          this.e9_1 = 1;
          this.v50_1.w4y(0, true);
          return Unit_instance;
        } else if (tmp === 1) {
          throw this.g9_1;
        }
      } catch ($p) {
        var e = $p;
        throw e;
      }
     while (true);
  };
  protoOf(EmojiSearch$slambda).g2e = function ($this$LaunchedEffect, completion) {
    var i = new EmojiSearch$slambda(this.v50_1, completion);
    i.w50_1 = $this$LaunchedEffect;
    return i;
  };
  function EmojiSearch$slambda_0($lazyListState, resultContinuation) {
    var i = new EmojiSearch$slambda($lazyListState, resultContinuation);
    var l = function ($this$LaunchedEffect, $completion) {
      return i.f2e($this$LaunchedEffect, $completion);
    };
    l.$arity = 1;
    return l;
  }
  function EmojiSearch$slambda_1($httpClient, $allEmojis, $refreshing$delegate, resultContinuation) {
    this.f51_1 = $httpClient;
    this.g51_1 = $allEmojis;
    this.h51_1 = $refreshing$delegate;
    CoroutineImpl.call(this, resultContinuation);
  }
  protoOf(EmojiSearch$slambda_1).f2e = function ($this$LaunchedEffect, $completion) {
    var tmp = this.g2e($this$LaunchedEffect, $completion);
    tmp.f9_1 = Unit_instance;
    tmp.g9_1 = null;
    return tmp.l9();
  };
  protoOf(EmojiSearch$slambda_1).x9 = function (p1, $completion) {
    return this.f2e((!(p1 == null) ? isInterface(p1, CoroutineScope) : false) ? p1 : THROW_CCE(), $completion);
  };
  protoOf(EmojiSearch$slambda_1).l9 = function () {
    var suspendResult = this.f9_1;
    $sm: do
      try {
        var tmp = this.d9_1;
        switch (tmp) {
          case 0:
            this.e9_1 = 5;
            this.d9_1 = 1;
            continue $sm;
          case 1:
            this.e9_1 = 4;
            EmojiSearch$lambda_2(this.h51_1, true);
            this.d9_1 = 2;
            suspendResult = this.f51_1.i50('https://api.github.com/emojis', mapOf(to('Accept', 'application/vnd.github.v3+json')), this);
            if (suspendResult === get_COROUTINE_SUSPENDED()) {
              return suspendResult;
            }

            continue $sm;
          case 2:
            var emojisJson = suspendResult;
            var this_0 = Default_getInstance();
            var this_1 = this_0.am();
            var this_2 = serializer(this_1, createKType(getKClass(KtMap), arrayOf([createInvariantKTypeProjection(createKType(PrimitiveClasses_getInstance().vb(), arrayOf([]), false)), createInvariantKTypeProjection(createKType(PrimitiveClasses_getInstance().vb(), arrayOf([]), false))]), false));
            var labelToUrl = this_0.v16(isInterface(this_2, KSerializer) ? this_2 : THROW_CCE(), emojisJson);
            this.g51_1.g2();
            var index = 0;
            var tmp_0 = this;
            var destination = ArrayList_init_$Create$_0(labelToUrl.m());
            var _iterator__ex2g4s = labelToUrl.t().j();
            while (_iterator__ex2g4s.k()) {
              var item = _iterator__ex2g4s.l();
              var key = item.x1();
              var value = item.y1();
              var _unary__edvuaz = index;
              index = _unary__edvuaz + 1 | 0;
              destination.e(new EmojiImage('' + _unary__edvuaz + '. ' + key, value));
            }

            tmp_0.j51_1 = this.g51_1.e3x(destination);
            this.e9_1 = 5;
            this.d9_1 = 3;
            continue $sm;
          case 3:
            this.j51_1;
            this.e9_1 = 5;
            EmojiSearch$lambda_2(this.h51_1, false);
            return Unit_instance;
          case 4:
            this.e9_1 = 5;
            var t = this.g9_1;
            EmojiSearch$lambda_2(this.h51_1, false);
            throw t;
          case 5:
            throw this.g9_1;
        }
      } catch ($p) {
        var e = $p;
        if (this.e9_1 === 5) {
          throw e;
        } else {
          this.d9_1 = this.e9_1;
          this.g9_1 = e;
        }
      }
     while (true);
  };
  protoOf(EmojiSearch$slambda_1).g2e = function ($this$LaunchedEffect, completion) {
    var i = new EmojiSearch$slambda_1(this.f51_1, this.g51_1, this.h51_1, completion);
    i.i51_1 = $this$LaunchedEffect;
    return i;
  };
  function EmojiSearch$slambda_2($httpClient, $allEmojis, $refreshing$delegate, resultContinuation) {
    var i = new EmojiSearch$slambda_1($httpClient, $allEmojis, $refreshing$delegate, resultContinuation);
    var l = function ($this$LaunchedEffect, $completion) {
      return i.f2e($this$LaunchedEffect, $completion);
    };
    l.$arity = 1;
    return l;
  }
  function EmojiSearch$lambda$lambda$slambda(resultContinuation) {
    CoroutineImpl.call(this, resultContinuation);
  }
  protoOf(EmojiSearch$lambda$lambda$slambda).f2e = function ($this$launch, $completion) {
    var tmp = this.g2e($this$launch, $completion);
    tmp.f9_1 = Unit_instance;
    tmp.g9_1 = null;
    return tmp.l9();
  };
  protoOf(EmojiSearch$lambda$lambda$slambda).x9 = function (p1, $completion) {
    return this.f2e((!(p1 == null) ? isInterface(p1, CoroutineScope) : false) ? p1 : THROW_CCE(), $completion);
  };
  protoOf(EmojiSearch$lambda$lambda$slambda).l9 = function () {
    var suspendResult = this.f9_1;
    $sm: do
      try {
        var tmp = this.d9_1;
        if (tmp === 0) {
          this.e9_1 = 1;
          throw RuntimeException_init_$Create$('boom!');
        } else if (tmp === 1) {
          throw this.g9_1;
        }
      } catch ($p) {
        var e = $p;
        throw e;
      }
     while (true);
  };
  protoOf(EmojiSearch$lambda$lambda$slambda).g2e = function ($this$launch, completion) {
    var i = new EmojiSearch$lambda$lambda$slambda(completion);
    i.s51_1 = $this$launch;
    return i;
  };
  function EmojiSearch$lambda$lambda$slambda_0(resultContinuation) {
    var i = new EmojiSearch$lambda$lambda$slambda(resultContinuation);
    var l = function ($this$launch, $completion) {
      return i.f2e($this$launch, $completion);
    };
    l.$arity = 1;
    return l;
  }
  function ComposableLambda$invoke$ref_0($boundThis) {
    return function (p0, p1, p2) {
      return $boundThis.r3x(p0, p1, p2);
    };
  }
  function ComposableLambda$invoke$ref_1($boundThis) {
    return function (p0, p1) {
      return $boundThis.y2t(p0, p1);
    };
  }
  function ComposableLambda$invoke$ref_2($boundThis) {
    return function (p0, p1, p2) {
      return $boundThis.r3x(p0, p1, p2);
    };
  }
  function Item$lambda() {
    _init_properties_EmojiSearch_kt__h8cahw();
    return Unit_instance;
  }
  function ComposableLambda$invoke$ref_3($boundThis) {
    return function (p0, p1, p2) {
      return $boundThis.r3x(p0, p1, p2);
    };
  }
  var properties_initialized_EmojiSearch_kt_kzucw2;
  function _init_properties_EmojiSearch_kt__h8cahw() {
    if (!properties_initialized_EmojiSearch_kt_kzucw2) {
      properties_initialized_EmojiSearch_kt_kzucw2 = true;
      loadingEmojiImage = new EmojiImage('loading\u2026', 'https://github.githubassets.com/images/icons/emoji/unicode/231a.png?v8');
      com_example_redwood_emojisearch_presenter_EmojiImage$stable = 0;
    }
  }
  var com_example_redwood_emojisearch_presenter_EmojiSearchTreehouseUi$stable;
  function ComposableLambda$invoke$ref_4($boundThis) {
    return function (p0, p1, p2) {
      return $boundThis.r3x(p0, p1, p2);
    };
  }
  function EmojiSearchTreehouseUi(httpClient, navigator) {
    this.t51_1 = httpClient;
    this.u51_1 = navigator;
  }
  protoOf(EmojiSearchTreehouseUi).k43 = function ($composer, $changed) {
    var $composer_0 = $composer;
    $composer_0.d2u(-503422726);
    if (isTraceInProgress()) {
      traceEventStart(-503422726, $changed, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi.Show (EmojiSearchTreehouseUi.kt:26)');
    }
    // Inline function 'kotlin.run' call
    // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi.Show.<anonymous>' call
    var dispatchReceiver = rememberComposableLambda(-1414259674, true, function (insets, $composer, $changed) {
      var $composer_0 = $composer;
      var $dirty = $changed;
      var tmp;
      if (($changed & 6) === 0) {
        $dirty = $dirty | ($composer_0.m2l(insets) ? 4 : 2);
        tmp = Unit_instance;
      }
      var tmp_0;
      if (!(($dirty & 19) === 18) || !$composer_0.l2r()) {
        if (isTraceInProgress()) {
          traceEventStart(-1414259674, $dirty, -1, 'com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi.Show.<anonymous> (EmojiSearchTreehouseUi.kt:28)');
        }
        EmojiSearch(this.t51_1, this.u51_1, null, insets, $composer_0, 7168 & $dirty << 9, 4);
        var tmp_1;
        if (isTraceInProgress()) {
          traceEventEnd();
          tmp_1 = Unit_instance;
        }
        tmp_0 = tmp_1;
      } else {
        $composer_0.m2o();
        tmp_0 = Unit_instance;
      }
      return Unit_instance;
    }.bind(this), $composer_0, 54);
    // Inline function 'androidx.compose.runtime.remember' call
    var $composer_1 = $composer_0;
    sourceInformationMarkerStart($composer_1, 1157296644, 'CC(remember)P(1):Composables.kt#9igjgp');
    // Inline function 'androidx.compose.runtime.cache' call
    var invalid = $composer_1.m2l(dispatchReceiver);
    // Inline function 'kotlin.let' call
    // Inline function 'androidx.compose.runtime.cache.<anonymous>' call
    var it = $composer_1.t2v();
    var tmp;
    if (invalid || it === Companion_getInstance().v2o_1) {
      // Inline function 'com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi.Show.<anonymous>.<anonymous>' call
      var value = ComposableLambda$invoke$ref_4(dispatchReceiver);
      $composer_1.e2w(value);
      tmp = value;
    } else {
      tmp = it;
    }
    var tmp_0 = tmp;
    var tmp0 = (tmp_0 == null ? true : !(tmp_0 == null)) ? tmp_0 : THROW_CCE();
    sourceInformationMarkerEnd($composer_1);
    ConsumeInsets(null, tmp0, $composer_0, 48, 1);
    if (isTraceInProgress()) {
      traceEventEnd();
    }
    $composer_0.f2u();
  };
  //region block: post-declaration
  protoOf(EmojiSearchTreehouseUi).u2m = close;
  //endregion
  //region block: init
  com_example_redwood_emojisearch_presenter_EmojiSearchTreehouseUi$stable = 0;
  //endregion
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = EmojiSearchTreehouseUi;
  _.$_$.b = HttpClient;
  //endregion
  return _;
}));

//# sourceMappingURL=redwood-samples-emoji-search-presenter.js.map
