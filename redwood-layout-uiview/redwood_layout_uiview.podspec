Pod::Spec.new do |spec|
    spec.name                     = 'redwood_layout_uiview'
    spec.version                  = '0.3.0-SNAPSHOT'
    spec.homepage                 = 'Link to a Kotlin/Native module homepage'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Some description for a Kotlin/Native module'
    spec.vendored_frameworks      = 'build/cocoapods/framework/redwood_layout_uiview.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '14.2'
    spec.dependency 'YogaKit', '~> 1.18'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':redwood-layout-uiview',
        'PRODUCT_MODULE_NAME' => 'redwood_layout_uiview',
    }
                
    spec.script_phases = [
        {
            :name => 'Build redwood_layout_uiview',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end