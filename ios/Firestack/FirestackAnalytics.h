//
//  FirestackAnalytics.h
//  Firestack
//
//  Created by Ari Lerner on 8/23/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef FirestackAnalytics_h
#define FirestackAnalytics_h

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif

@interface FirestackAnalytics : NSObject <RCTBridgeModule> {
    
}

@end

#endif