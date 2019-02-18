//
//  FirestackAuth.h
//  Firestack
//
//  Created by Ari Lerner on 8/23/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef FirestackAuth_h
#define FirestackAuth_h

#import "Firebase.h"
#import "RCTEventEmitter.h"
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif

@interface FirestackAuth : RCTEventEmitter <RCTBridgeModule> {
    FIRAuthStateDidChangeListenerHandle authListenerHandle;
    Boolean listening;
}

@end

#endif