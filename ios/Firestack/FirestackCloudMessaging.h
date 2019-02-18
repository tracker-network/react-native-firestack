//
//  FirestackMessaging.h
//  Firestack
//
//  Created by Ari Lerner on 8/23/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef FirestackCloudMessaging_h
#define FirestackCloudMessaging_h

#import "Firebase.h"
#import "RCTEventEmitter.h"
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif
#import "RCTUtils.h"

@interface FirestackCloudMessaging : RCTEventEmitter <RCTBridgeModule> {
    
}

+ (void) setup:(UIApplication *)application;

@end

#endif